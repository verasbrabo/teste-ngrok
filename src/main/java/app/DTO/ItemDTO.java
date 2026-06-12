package app.DTO;

import java.math.BigDecimal;
import java.util.List;

import app.entity.Item;
import jakarta.validation.constraints.NotNull;

public record ItemDTO(
		Long id,
		String nome,
		String tipo,
		String descricao,
		@NotNull
		List<IngredienteDTO> ingredientes,
		List<AcompanhamentoViewDTO> acompanhamentos,
		BigDecimal valorBase,
		BigDecimal valorPromo,
		String imagem,
		Long ordem) {

	public static ItemDTO from(Item i) {
	    return new ItemDTO(
	    	i.getId(),
	        i.getNome(),
	        i.getTipo(),
	        i.getDescricao(),
	        i.getIngredientes().stream().map(IngredienteDTO::from).toList(),
	        i.getAcompanhamentos().stream().map(AcompanhamentoViewDTO::from).toList(),
	        i.getValorBase(),
	        i.getValorPromo(),
	        i.getImagem() != null ? i.getImagem().getUrl() : null,
	        i.getOrdem()
	    );
	}
	
	public static ItemDTO getFrom(Item i) {
	    return new ItemDTO(
	    	i.getId(),
	        i.getNome(),
	        i.getTipo(),
	        i.getDescricao(),
	        i.getIngredientes().stream().map(IngredienteDTO::from).toList(),
	        i.getAcompanhamentos().stream().map(AcompanhamentoViewDTO::from).toList(),
	        i.getValorBase(),
	        i.getValorPromo(),
	        i.getImagem() != null ? i.getImagem().getUrl() : null,
	        null
	    );
	}
}
