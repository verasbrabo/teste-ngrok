package app.DTO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import app.entity.Item;
import jakarta.validation.constraints.NotNull;

public record ItemDTO(
		Long id,
		String nome,
		String descricao,
		@NotNull
		List<String> ingredientes,
		List<AcompanhamentoViewDTO> acompanhamentos,
		BigDecimal valorBase,
		BigDecimal valorPromo,
		String imagem,
		Long ordem) {

	public static ItemDTO from(Item i) {
	    return new ItemDTO(
	    	i.getId(),
	        i.getNome(),
	        i.getDescricao(),
//	        i.getIngredientes().stream().map(IngredienteDTO::from).toList(),
	        Arrays.asList(i.getIngredientes().split(";")),
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
	        i.getDescricao(),
	        Arrays.asList(i.getIngredientes().split(";")),
	        i.getAcompanhamentos().stream().map(AcompanhamentoViewDTO::from).toList(),
	        i.getValorBase(),
	        i.getValorPromo(),
	        i.getImagem() != null ? i.getImagem().getUrl() : null,
	        null
	    );
	}
}
