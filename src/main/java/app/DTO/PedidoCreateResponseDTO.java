package app.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import app.entity.Pedido;
import app.enums.StatusPedido;

public record PedidoCreateResponseDTO(
		UUID id,
		BigDecimal valorTotal,
		LocalDateTime dataHoraPedido,
		Long tempoEstimado,
		String quiosque,
		String entregador,
		StatusPedido status
		) {
	
	public static PedidoCreateResponseDTO from(Pedido p) {
	    return new PedidoCreateResponseDTO(
	    	p.getId(),
	        p.getValorTotal(),
	        p.getDataHoraPedido(),
	        p.getTempoEstimado(),
	        p.getQuiosque().getNome(),
	        p.getEntregador() != null ? p.getEntregador().getNome() : null,
	        p.getStatus()
	    );
	}
}
