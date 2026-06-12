package app.DTO;

import java.util.Comparator;
import java.util.List;

import app.entity.Categoria;
import app.entity.Item;

public record CategoriaViewDTO(
		Long id_categoria,
		String nome,
		Integer ordem,
		List<ItemDTO> itens) {

	
	public static CategoriaViewDTO from(Categoria c) {
	    return new CategoriaViewDTO(
	        c.getId(),
	        c.getNome().getCategorias(),
	        c.getOrdem(), 
	        // Só itens ativos no cardápio (soft delete); `ordem` pode ser nula
	        // (itens criados pelo app), e nulos vão pro fim.
	        c.getItens().stream()
	            .filter(Item::isAtivo)
	            .sorted(Comparator.comparing(Item::getOrdem,
	                Comparator.nullsLast(Comparator.naturalOrder())))
	            .map(ItemDTO::from).toList()
	    );
	}
}
