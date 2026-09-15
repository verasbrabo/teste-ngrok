package app.DTO;

import java.util.List;
import java.util.UUID;

import app.entity.Pedido;
import app.entity.PontoRastreamentoEntrega;
import app.enums.StatusPedido;

/**
 * Foto inicial do rastreamento usada antes de abrir o canal SSE.
 */
public record RastreamentoEntregaDTO(
		UUID pedidoId,
		StatusPedido statusPedido,
		Double destinoLatitude,
		Double destinoLongitude,
		List<PontoRastreamentoEntregaDTO> pontos) {

	public static RastreamentoEntregaDTO from(
			Pedido pedido,
			List<PontoRastreamentoEntrega> pontos) {
		var destino = pedido.getCoordenada();
		return new RastreamentoEntregaDTO(
				pedido.getId(),
				pedido.getStatus(),
				destino != null ? destino.getLatitude() : null,
				destino != null ? destino.getLongitude() : null,
				pontos.stream().map(PontoRastreamentoEntregaDTO::from).toList());
	}
}
