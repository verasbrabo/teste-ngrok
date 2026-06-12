package app.DTO;

import app.entity.Categoria;


public record CategoriaCreateDTO(String nome, Integer ordem) {

	
	public static CategoriaCreateDTO from(Categoria c) {
	    return new CategoriaCreateDTO(
	        c.getNome().getCategorias(),
	        c.getOrdem()
	    );
	}
}
