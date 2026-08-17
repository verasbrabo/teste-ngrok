package app.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import app.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PedidoDTO(
        // UUID do aparelho do cliente. Vai para Pedido.IdDevice e depois vira
        // o service UUID que o quiosque escaneia por BLE.
        UUID idAparelho,
        // Intencao de pagamento escolhida no app antes do POST /pedidos. Campo
        // obrigatorio para impedir pedido sem orientacao de recebimento.
        @NotNull
        FormaPagamento formaPagamento,
        // Obrigatorio quando a forma for DINHEIRO. O cliente informa quanto vai
        // entregar; a API recalcula o troco a partir do valor total real.
        BigDecimal valorPagoCliente,
		@NotNull
	    Long quiosque,
	    //@NotNull
	    //LocalDateTime dataHoraPedido,
	    @NotNull
	    List<@Valid ItemPedidoDTO> itens,
	    @NotNull
	    double latitudeEntrega,
	    @NotNull
	    double longitudeEntrega,
	    String codigoEntrega
	) {}
