package app.DTO;

import app.enums.StatusPedido;
import java.util.UUID;

public record AtualizarStatusDTO(
        StatusPedido status,
        // UUID do aparelho do entregador enviado quando o pedido vai para
        // EM_ENTREGA. O detector escaneia o UUID do cliente, nao este campo.
        UUID idAparelhoEntregador) {

}
