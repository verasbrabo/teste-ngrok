package app.DTO;

import app.entity.ItemIngrediente;

public record IngredienteDTO(
		Long id,
		String nome
		) {
	public static IngredienteDTO from(ItemIngrediente i) {
		return new IngredienteDTO(
				i.getIngrediente().getId(),
				i.getIngrediente().getNome()
		);
	}
}
