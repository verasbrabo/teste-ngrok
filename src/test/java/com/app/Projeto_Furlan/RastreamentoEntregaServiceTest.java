package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.LocalizacaoEntregaCreateDTO;
import app.entity.Pedido;
import app.entity.PontoRastreamentoEntrega;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusPedido;
import app.repository.PedidoRepository;
import app.repository.PontoRastreamentoEntregaRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class RastreamentoEntregaServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@Mock
	private PontoRastreamentoEntregaRepository pontoRepository;

	@InjectMocks
	private RastreamentoEntregaService rastreamentoService;

	private final Long quiosqueId = 22L;
	private final UUID pedidoId = UUID.randomUUID();
	private final UUID aparelhoEntregadorId = UUID.randomUUID();
	private Usuario funcionario;
	private Quiosque quiosque;
	private Pedido pedido;

	@BeforeEach
	void prepararEntrega() {
		funcionario = org.mockito.Mockito.mock(Usuario.class);
		quiosque = org.mockito.Mockito.mock(Quiosque.class);
		pedido = new Pedido();
		pedido.setId(pedidoId);
		pedido.setStatus(StatusPedido.EM_ENTREGA);
		pedido.setIdAparelhoEntregador(aparelhoEntregadorId);
	}

	@Test
	void deveSalvarLocalizacaoDoAparelhoQueIniciouAEntrega() {
		prepararAcessoAoPedido();
		when(pontoRepository.findFirstByPedidoOrderByCapturadoEmDesc(pedido))
				.thenReturn(Optional.empty());
		when(pontoRepository.save(any(PontoRastreamentoEntrega.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));
		Instant capturadoEm = Instant.now();

		rastreamentoService.publicar(
				funcionario,
				quiosqueId,
				pedidoId,
				localizacao(-23.55052, -46.633308, capturadoEm, aparelhoEntregadorId));

		ArgumentCaptor<PontoRastreamentoEntrega> captor =
				ArgumentCaptor.forClass(PontoRastreamentoEntrega.class);
		verify(pontoRepository).save(captor.capture());
		assertEquals(pedido, captor.getValue().getPedido());
		assertEquals(-23.55052, captor.getValue().getLatitude());
		assertEquals(capturadoEm, captor.getValue().getCapturadoEm());
	}

	@Test
	void deveRejeitarPublicacaoFeitaPorOutroAparelho() {
		prepararAcessoAoPedido();

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> rastreamentoService.publicar(
						funcionario,
						quiosqueId,
						pedidoId,
						localizacao(
								-23.55052,
								-46.633308,
								Instant.now(),
								UUID.randomUUID())));

		assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
		verifyNoInteractions(pontoRepository);
	}

	@Test
	void deveIgnorarPontoEstacionadoRecebidoAntesDeQuinzeSegundos() {
		prepararAcessoAoPedido();
		Instant anteriorEm = Instant.now().minusSeconds(5);
		PontoRastreamentoEntrega anterior = new PontoRastreamentoEntrega(
				pedido, -23.55052, -46.633308, 8.0, 0.0, 0.0, anteriorEm);
		when(pontoRepository.findFirstByPedidoOrderByCapturadoEmDesc(pedido))
				.thenReturn(Optional.of(anterior));

		rastreamentoService.publicar(
				funcionario,
				quiosqueId,
				pedidoId,
				localizacao(
						-23.55052,
						-46.633308,
						anteriorEm.plusSeconds(5),
						aparelhoEntregadorId));

		verify(pontoRepository, never()).save(any(PontoRastreamentoEntrega.class));
	}

	@Test
	void clienteNaoPodeConsultarRastreamentoDeOutroPedido() {
		Usuario outroCliente = org.mockito.Mockito.mock(Usuario.class);
		when(pedidoRepository.findByClienteAndId(outroCliente, pedidoId))
				.thenReturn(Optional.empty());

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> rastreamentoService.buscar(outroCliente, pedidoId));

		assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
		verifyNoInteractions(pontoRepository);
	}

	private void prepararAcessoAoPedido() {
		when(quiosqueRepository.findById(quiosqueId)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(funcionario)).thenReturn(true);
		when(pedidoRepository.findByIdAndQuiosque(pedidoId, quiosque))
				.thenReturn(Optional.of(pedido));
	}

	private LocalizacaoEntregaCreateDTO localizacao(
			double latitude,
			double longitude,
			Instant capturadoEm,
			UUID aparelhoId) {
		return new LocalizacaoEntregaCreateDTO(
				latitude,
				longitude,
				8.0,
				180.0,
				4.5,
				capturadoEm,
				aparelhoId);
	}
}
