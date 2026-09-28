package app.DTO;

import app.repository.AvaliacaoProjection;

public record AvaliacaoDTO(
		Integer nota,
		String comentario
		) {

	
	public static AvaliacaoDTO from(AvaliacaoProjection ap) {
		return new AvaliacaoDTO(
				ap.getNota(),
				ap.getComentario()
				);
	}
	
}
