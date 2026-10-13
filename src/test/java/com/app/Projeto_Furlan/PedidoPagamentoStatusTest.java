package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.entity.Pedido;
import app.enums.StatusPagamento;
import app.enums.StatusPedido;

class PedidoPagamentoStatusTest {

	@Test
	void deveMarcarPagamentoComoPagoQuandoPedidoForFinalizado() {
		Pedido pedido = pedidoPendente(StatusPedido.FINALIZADO);

		PedidoService.sincronizarStatusPagamento(pedido);

		assertEquals(StatusPagamento.PAGO, pedido.getStatusPagamento());
	}

	@Test
	void deveCancelarPagamentoQuandoPedidoForCancelado() {
		Pedido pedido = pedidoPendente(StatusPedido.CANCELADO);

		PedidoService.sincronizarStatusPagamento(pedido);

		assertEquals(StatusPagamento.CANCELADO, pedido.getStatusPagamento());
	}

	@Test
	void deveCancelarPagamentoQuandoPedidoForRejeitado() {
		Pedido pedido = pedidoPendente(StatusPedido.REJEITADO);

		PedidoService.sincronizarStatusPagamento(pedido);

		assertEquals(StatusPagamento.CANCELADO, pedido.getStatusPagamento());
	}

	@Test
	void deveManterPagamentoPendenteEnquantoPedidoEstiverEmAndamento() {
		Pedido pedido = pedidoPendente(StatusPedido.EM_ENTREGA);

		PedidoService.sincronizarStatusPagamento(pedido);

		assertEquals(StatusPagamento.PENDENTE, pedido.getStatusPagamento());
	}

	private Pedido pedidoPendente(StatusPedido statusPedido) {
		Pedido pedido = new Pedido();
		pedido.setStatus(statusPedido);
		pedido.setStatusPagamento(StatusPagamento.PENDENTE);
		return pedido;
	}
}
