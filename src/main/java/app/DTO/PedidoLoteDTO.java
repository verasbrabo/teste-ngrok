package app.DTO;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/** Agrupa todos os pedidos do carrinho em uma unica transacao. */
public record PedidoLoteDTO(
		@NotEmpty(message = "Informe ao menos um pedido")
		List<@Valid PedidoDTO> pedidos
) {
}
