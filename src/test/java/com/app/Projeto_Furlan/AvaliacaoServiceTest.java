package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.AvaliarDTO;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusPedido;
import app.enums.UserRole;
import app.repository.PedidoRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@InjectMocks
	private PedidoService pedidoService;

	private final UUID pedidoId = UUID.randomUUID();
	private Usuario cliente;
	private Pedido pedido;
	private Quiosque quiosque;

	@BeforeEach
	void preparar() {
		cliente = mock(Usuario.class);
		quiosque = mock(Quiosque.class);
		pedido = new Pedido();
		pedido.setId(pedidoId);
		pedido.setCliente(cliente);
		pedido.setQuiosque(quiosque);
		when(cliente.getRole()).thenReturn(UserRole.CLIENTE);
	}

	@Test
	void deveAvaliarPedidoFinalizadoComComentarioAnonimo() {
		pedido.setStatus(StatusPedido.FINALIZADO);
		when(pedidoRepository.findByClienteAndId(cliente, pedidoId))
				.thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		pedidoService.avaliarPedido(
				cliente, pedidoId, new AvaliarDTO(5, "  Otimo atendimento  "));

		assertEquals(StatusPedido.AVALIADO, pedido.getStatus());
		assertEquals(5, pedido.getNota());
		assertEquals("Otimo atendimento", pedido.getComentario());
		verify(quiosque).addAvaliacao(5);
		verify(quiosqueRepository).save(quiosque);
	}

	@Test
	void devePermitirAvaliacaoSemComentario() {
		pedido.setStatus(StatusPedido.FINALIZADO);
		when(pedidoRepository.findByClienteAndId(cliente, pedidoId))
				.thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		pedidoService.avaliarPedido(cliente, pedidoId, new AvaliarDTO(4, null));

		assertEquals(StatusPedido.AVALIADO, pedido.getStatus());
		assertEquals(4, pedido.getNota());
		assertNull(pedido.getComentario());
		verify(quiosque).addAvaliacao(4);
	}

	@Test
	void deveImpedirAvaliacaoDuplicada() {
		pedido.setStatus(StatusPedido.AVALIADO);
		pedido.setNota(5);
		when(pedidoRepository.findByClienteAndId(cliente, pedidoId))
				.thenReturn(Optional.of(pedido));

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.avaliarPedido(
						cliente, pedidoId, new AvaliarDTO(3, "Nova tentativa")));

		assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
		verify(quiosque, never()).addAvaliacao(3);
		verify(quiosqueRepository, never()).save(quiosque);
	}

	@Test
	void deveRejeitarComentarioComLink() {
		pedido.setStatus(StatusPedido.FINALIZADO);
		when(pedidoRepository.findByClienteAndId(cliente, pedidoId))
				.thenReturn(Optional.of(pedido));

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> pedidoService.avaliarPedido(
						cliente, pedidoId, new AvaliarDTO(5, "Veja www.exemplo.com")));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
		verify(quiosque, never()).addAvaliacao(5);
	}
}
