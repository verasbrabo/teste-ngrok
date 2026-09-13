package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.ItemPedidoDTO;
import app.DTO.MotivoCancelamentoDTO;
import app.DTO.PedidoDTO;
import app.DTO.PedidoGetDTO;
import app.DTO.PedidoLoteDTO;
import app.auth.AuthRepository;
import app.config.NotificationRequestDTO;
import app.config.NotificationService;
import app.entity.Cliente;
import app.entity.Coordenada;
import app.entity.Cupom;
import app.entity.Item;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.FormaPagamento;
import app.enums.StatusPedido;
import app.enums.TipoCupom;
import app.repository.AcompanhamentoRepository;
import app.repository.CupomRepository;
import app.repository.ItemRepository;
import app.repository.PedidoRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@ExtendWith(MockitoExtension.class)
class PedidoTransacaoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;
	@Mock
	private QuiosqueRepository quiosqueRepository;
	@Mock
	private ItemRepository itemRepository;
	@Mock
	private AcompanhamentoRepository acompanhamentoRepository;
	@Mock
	private CupomRepository cupomRepository;
	@Mock
	private CupomClienteRepository cupomClienteRepository;
	@Mock
	private AuthRepository userRepository;
	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private PedidoService pedidoService;

	@Test
	void deveRejeitarItensDeQuiosquesDiferentesNoMesmoPedido() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Item itemCorreto = item(101L, "50.00");
		Item itemDeOutroQuiosque = item(202L, "30.00");
		when(itemRepository.findAllById(List.of(101L, 202L)))
				.thenReturn(List.of(itemCorreto, itemDeOutroQuiosque));
		when(itemCorreto.pertenceAoQuiosque(quiosque)).thenReturn(true);
		when(itemDeOutroQuiosque.pertenceAoQuiosque(quiosque)).thenReturn(false);

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.createMeuPedido(
						cliente,
						pedido(10L, null, List.of(itemPedido(101L), itemPedido(202L)))));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
		verify(pedidoRepository, never()).save(any(Pedido.class));
		verifyNoInteractions(cupomClienteRepository);
	}

	@Test
	void deveRejeitarCupomDeOutroQuiosque() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Item item = item(101L, "100.00");
		when(itemRepository.findAllById(List.of(101L))).thenReturn(List.of(item));
		when(item.pertenceAoQuiosque(quiosque)).thenReturn(true);
		when(cupomRepository.findByIdAndQuiosqueId(8L, 10L))
				.thenReturn(Optional.empty());

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.createMeuPedido(
						cliente, pedido(10L, 8L, List.of(itemPedido(101L)))));

		assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
		verifyNoInteractions(cupomClienteRepository);
		verify(pedidoRepository, never()).save(any(Pedido.class));
	}

	@Test
	void deveRejeitarCupomJaUtilizadoSemSalvarPedido() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Item item = item(101L, "100.00");
		Cupom cupom = mock(Cupom.class);
		when(itemRepository.findAllById(List.of(101L))).thenReturn(List.of(item));
		when(item.pertenceAoQuiosque(quiosque)).thenReturn(true);
		when(cupomRepository.findByIdAndQuiosqueId(8L, 10L))
				.thenReturn(Optional.of(cupom));
		when(cupom.podeSerAplicado(new BigDecimal("100.00"))).thenReturn(true);
		when(cupomClienteRepository.existsByCupomIdAndClienteId(8L, 7L)).thenReturn(true);

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.createMeuPedido(
						cliente, pedido(10L, 8L, List.of(itemPedido(101L)))));

		assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
		verify(cupomClienteRepository, never()).save(any());
		verify(pedidoRepository, never()).save(any(Pedido.class));
	}

	@Test
	void deveCalcularDescontoSomenteComSubtotalDoQuiosqueDoPedido() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Item item = item(101L, "100.00");
		Cupom cupom = new Cupom(
				TipoCupom.FIXO,
				quiosque,
				new BigDecimal("10.00"),
				new BigDecimal("50.00"),
				null,
				LocalDateTime.now().plusDays(1),
				true);
		when(itemRepository.findAllById(List.of(101L))).thenReturn(List.of(item));
		when(item.pertenceAoQuiosque(quiosque)).thenReturn(true);
		when(cupomRepository.findByIdAndQuiosqueId(8L, 10L))
				.thenReturn(Optional.of(cupom));
		when(cupomClienteRepository.existsByCupomIdAndClienteId(8L, 7L)).thenReturn(false);
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocacao -> {
			Pedido salvo = invocacao.getArgument(0);
			salvo.setId(UUID.randomUUID());
			return salvo;
		});

		PedidoGetDTO resultado = pedidoService.createMeuPedido(
				cliente, pedido(10L, 8L, List.of(itemPedido(101L))));

		assertEquals(0, new BigDecimal("100.00").compareTo(resultado.subTotal()));
		assertEquals(0, new BigDecimal("10.00").compareTo(resultado.desconto()));
		assertEquals(0, new BigDecimal("90.00").compareTo(resultado.valorTotal()));
		verify(cupomClienteRepository).save(any());
	}

	@Test
	void deveRejeitarQuiosqueDuplicadoAntesDeIniciarOLote() {
		PedidoDTO primeiro = pedido(10L, null, List.of(itemPedido(101L)));
		PedidoDTO segundo = pedido(10L, null, List.of(itemPedido(102L)));

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.createPedidosEmLote(
						cliente(), new PedidoLoteDTO(List.of(primeiro, segundo))));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
		verifyNoInteractions(quiosqueRepository, pedidoRepository, cupomClienteRepository);
	}

	@Test
	void devePropagarFalhaDoSegundoPedidoParaORollbackDoLote() {
		PedidoService servico = spy(new PedidoService());
		Cliente cliente = mock(Cliente.class);
		PedidoDTO primeiro = pedido(10L, null, List.of(itemPedido(101L)));
		PedidoDTO segundo = pedido(20L, null, List.of(itemPedido(202L)));
		PedidoGetDTO criado = mock(PedidoGetDTO.class);
		ResponseStatusException falha = new ResponseStatusException(
				HttpStatus.CONFLICT, "Cupom invalido no segundo quiosque");
		doReturn(criado).when(servico).createMeuPedido(cliente, primeiro);
		doThrow(falha).when(servico).createMeuPedido(cliente, segundo);

		ResponseStatusException recebida = assertThrows(
				ResponseStatusException.class,
				() -> servico.createPedidosEmLote(
						cliente, new PedidoLoteDTO(List.of(primeiro, segundo))));

		assertSame(falha, recebida);
		verify(servico).createMeuPedido(cliente, primeiro);
		verify(servico).createMeuPedido(cliente, segundo);
	}

	@Test
	void loteDeveEstarProtegidoPorTransacao() throws NoSuchMethodException {
		Transactional anotacao = PedidoService.class
				.getMethod("createPedidosEmLote", Cliente.class, PedidoLoteDTO.class)
				.getAnnotation(Transactional.class);
		assertNotNull(anotacao);
	}

	@Test
	void deveNotificarQuiosqueSomenteDepoisDoCommit() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Item item = item(101L, "100.00");
		app.entity.Funcionario proprietario = mock(app.entity.Funcionario.class);
		when(proprietario.getDeviceToken()).thenReturn("token-proprietario");
		when(quiosque.getProprietario()).thenReturn(proprietario);
		when(itemRepository.findAllById(List.of(101L))).thenReturn(List.of(item));
		when(item.pertenceAoQuiosque(quiosque)).thenReturn(true);
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocacao -> {
			Pedido salvo = invocacao.getArgument(0);
			salvo.setId(UUID.randomUUID());
			return salvo;
		});

		TransactionSynchronizationManager.initSynchronization();
		try {
			pedidoService.createMeuPedido(
					cliente, pedido(10L, null, List.of(itemPedido(101L))));
			verify(notificationService, never())
					.sendNotificationToDevice(any(NotificationRequestDTO.class));

			for (TransactionSynchronization sincronizacao
					: TransactionSynchronizationManager.getSynchronizations()) {
				sincronizacao.afterCommit();
			}
			verify(notificationService)
					.sendNotificationToDevice(any(NotificationRequestDTO.class));
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	void deveDevolverCupomQuandoClienteCancelaPedido() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Pedido pedido = pedidoComCupom(cliente, quiosque, StatusPedido.CRIADO, 8L);
		when(pedidoRepository.findByClienteAndId(cliente, pedido.getId()))
				.thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		String status = pedidoService.cancelarPedido(cliente, pedido.getId());

		assertEquals(StatusPedido.CANCELADO.name(), status);
		verify(cupomClienteRepository).deleteByCupomIdAndClienteId(8L, 7L);
		assertNotNull(pedido.getCupom());
	}

	@Test
	void deveDevolverCupomQuandoQuiosqueCancelaPedido() {
		Cliente cliente = cliente();
		Usuario funcionario = mock(Usuario.class);
		Quiosque quiosque = prepararQuiosque(10L);
		when(quiosque.usuarioPossuiAcesso(funcionario)).thenReturn(true);
		Pedido pedido = pedidoComCupom(cliente, quiosque, StatusPedido.ACEITO, 8L);
		when(pedidoRepository.findByIdAndQuiosque(pedido.getId(), quiosque))
				.thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		pedidoService.quiosqueCancelarPedido(
				funcionario,
				10L,
				pedido.getId(),
				new MotivoCancelamentoDTO("Sem estoque"));

		assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
		verify(cupomClienteRepository).deleteByCupomIdAndClienteId(8L, 7L);
	}

	@Test
	void deveDevolverCupomQuandoQuiosqueRejeitaPedido() {
		Cliente cliente = cliente();
		Usuario funcionario = mock(Usuario.class);
		Quiosque quiosque = prepararQuiosque(10L);
		when(quiosque.usuarioPossuiAcesso(funcionario)).thenReturn(true);
		Pedido pedido = pedidoComCupom(cliente, quiosque, StatusPedido.CRIADO, 8L);
		when(pedidoRepository.findByIdAndQuiosque(pedido.getId(), quiosque))
				.thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		pedidoService.rejeitarPedido(funcionario, 10L, pedido.getId());

		assertEquals(StatusPedido.REJEITADO, pedido.getStatus());
		verify(cupomClienteRepository).deleteByCupomIdAndClienteId(8L, 7L);
	}

	@Test
	void naoDeveDevolverCupomNovamenteParaPedidoJaCancelado() {
		Cliente cliente = cliente();
		Quiosque quiosque = prepararQuiosque(10L);
		Pedido pedido = pedidoComCupom(cliente, quiosque, StatusPedido.CANCELADO, 8L);
		when(pedidoRepository.findByClienteAndId(cliente, pedido.getId()))
				.thenReturn(Optional.of(pedido));

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.cancelarPedido(cliente, pedido.getId()));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
		verify(cupomClienteRepository, never())
				.deleteByCupomIdAndClienteId(any(), any());
	}

	private Cliente cliente() {
		Cliente cliente = mock(Cliente.class);
		lenient().when(cliente.getId()).thenReturn(7L);
		lenient().when(cliente.getNome()).thenReturn("Cliente Teste");
		lenient().when(cliente.getRole()).thenReturn(app.enums.UserRole.CLIENTE);
		return cliente;
	}

	private Quiosque prepararQuiosque(Long id) {
		Quiosque quiosque = mock(Quiosque.class);
		lenient().when(quiosqueRepository.findById(id)).thenReturn(Optional.of(quiosque));
		lenient().when(quiosque.atende(any(Coordenada.class))).thenReturn(true);
		lenient().when(quiosque.getId()).thenReturn(id);
		lenient().when(quiosque.getNome()).thenReturn("Quiosque " + id);
		lenient().when(quiosque.calcularTempoEstimado()).thenReturn(0L);
		lenient().when(quiosque.getFuncionarios()).thenReturn(List.of());
		return quiosque;
	}

	private Item item(Long id, String valor) {
		Item item = mock(Item.class);
		lenient().when(item.getId()).thenReturn(id);
		lenient().when(item.getNome()).thenReturn("Item " + id);
		lenient().when(item.getValorFinal()).thenReturn(new BigDecimal(valor));
		return item;
	}

	private Pedido pedidoComCupom(
			Cliente cliente,
			Quiosque quiosque,
				StatusPedido status,
				Long cupomId) {
		Cupom cupom = mock(Cupom.class);
		lenient().when(cupom.getId()).thenReturn(cupomId);
		Pedido pedido = new Pedido(
				LocalDateTime.now(),
				null,
				cliente,
				quiosque,
				new java.util.ArrayList<>(),
				status,
				UUID.randomUUID());
		pedido.setId(UUID.randomUUID());
		pedido.setCupom(cupom);
		pedido.setPrecisaMaquininha(false);
		return pedido;
	}

	private ItemPedidoDTO itemPedido(Long id) {
		return new ItemPedidoDTO(id, 1, List.of(), List.of());
	}

	private static PedidoDTO pedido(Long quiosqueId, Long cupomId, List<ItemPedidoDTO> itens) {
		return new PedidoDTO(
				UUID.randomUUID(),
				FormaPagamento.PIX,
				null,
				quiosqueId,
				cupomId,
				itens,
				-23.0,
				-46.0,
				"ABCD");
	}
}
