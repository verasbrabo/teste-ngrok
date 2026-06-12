package app.DTO;

import java.math.BigDecimal;
import java.util.List;

import app.entity.Acompanhamento;
import app.entity.Ingrediente;
import app.entity.ItemPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoGetDTO(
	    @NotNull
	    @Positive
	    Long itemId,
	    String nome,
	    @NotNull
	    @Positive
	    Integer quantidade,
	    BigDecimal subTotal,
	    BigDecimal valorUnit,
	    List<String> acompanhamentosid,
	    // Acompanhamentos com preço (nome + valor). Mantemos `acompanhamentosid`
	    // (só os nomes) por compatibilidade com clientes existentes.
	    List<AcompanhamentoPedidoDTO> acompanhamentos,
	    List<String> ingredientesid
	) {

	public static ItemPedidoGetDTO from(ItemPedido ip) {
		return new ItemPedidoGetDTO(
				ip.getItem().getId(),
				ip.getItem().getNome(),
				ip.getQuantidade(),
				ip.getSubTotal(),
				ip.getValorUnit(),
				ip.getAcompanhamentos().stream().map(Acompanhamento::getNome).toList(),
				ip.getAcompanhamentos().stream().map(AcompanhamentoPedidoDTO::from).toList(),
				ip.getIngredientesRemovido().stream().map(Ingrediente::getNome).toList()
				);

	}

}
