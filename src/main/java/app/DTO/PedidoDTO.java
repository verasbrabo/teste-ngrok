package app.DTO;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PedidoDTO(
        // UUID do aparelho do cliente. Vai para Pedido.IdDevice e depois vira
        // o service UUID que o quiosque escaneia por BLE.
        UUID idAparelho,
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
