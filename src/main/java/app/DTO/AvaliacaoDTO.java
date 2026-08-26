package app.DTO;

import app.repository.AvaliacaoProjection;

public record AvaliacaoDTO(
		String nome,
		Integer nota,
		String comentario
		) {

	
	public static AvaliacaoDTO from(AvaliacaoProjection ap) {
		return new AvaliacaoDTO(
				ap.getNomeCliente(),
				ap.getNota(),
				ap.getComentario()
				);
	}
	
}
