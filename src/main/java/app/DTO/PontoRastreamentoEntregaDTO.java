package app.DTO;

import java.time.Instant;

import app.entity.PontoRastreamentoEntrega;

/**
 * Ponto seguro exposto aos aplicativos durante uma entrega.
 */
public record PontoRastreamentoEntregaDTO(
		Long id,
		Double latitude,
		Double longitude,
		Double precisaoMetros,
		Double direcaoGraus,
		Double velocidadeMetrosSegundo,
		Instant capturadoEm) {

	public static PontoRastreamentoEntregaDTO from(PontoRastreamentoEntrega ponto) {
		return new PontoRastreamentoEntregaDTO(
				ponto.getId(),
				ponto.getLatitude(),
				ponto.getLongitude(),
				ponto.getPrecisaoMetros(),
				ponto.getDirecaoGraus(),
				ponto.getVelocidadeMetrosSegundo(),
				ponto.getCapturadoEm());
	}
}
