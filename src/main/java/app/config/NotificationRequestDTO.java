package app.config;

import app.entity.Pedido;

public record NotificationRequestDTO(
		String token,
		String title,
		String body,
		String id,
		String status,
		String motivo,
		String tipo) {

	public static NotificationRequestDTO from(Pedido p) {
		return new NotificationRequestDTO(
				p.getCliente().getDeviceToken(),
				"Status do seu Pedido",
				p.getStatus().getDescricao(),
				p.getId().toString(),
				p.getStatus().name(),
				null,
				"STATUS_PEDIDO");
	}

	public static NotificationRequestDTO cancelado(Pedido p) {
		return new NotificationRequestDTO(
				p.getCliente().getDeviceToken(),
				"Status do seu Pedido",
				p.getStatus().getDescricao(),
				p.getId().toString(),
				p.getStatus().name(),
				p.getMotivoCancel(),
				"STATUS_PEDIDO");
	}

	public static NotificationRequestDTO entregador(Pedido p) {
		return new NotificationRequestDTO(
				p.getCliente().getDeviceToken(),
				p.getStatus().getDescricao(),
				p.getEntregador().getNome() + " irá entregar seu pedido.",
				p.getId().toString(),
				p.getStatus().name(),
				p.getMotivoCancel(),
				"STATUS_PEDIDO");
	}

	/** Aviso unico criado quando dois pontos de GPS confirmam a chegada. */
	public static NotificationRequestDTO entregadorProximo(Pedido p) {
		return new NotificationRequestDTO(
				p.getCliente().getDeviceToken(),
				"Entregador proximo",
				"Seu entregador esta proximo do local de entrega.",
				p.getId().toString(),
				p.getStatus().name(),
				null,
				"ENTREGADOR_PROXIMO");
	}

	/**
	 * Notificação para o app do quiosque (novo pedido recebido). Não se refere
	 * a uma mudança de status do pedido do cliente.
	 */
	public static NotificationRequestDTO novoPedido(String token, String titulo, String corpo, String idPedido) {
		return new NotificationRequestDTO(token, titulo, corpo, idPedido, null, null, "NOVO_PEDIDO");
	}
}
