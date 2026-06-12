package app.DTO;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record ItemPedidoDTO(
	    @NotNull
	    @Positive
	    Long itemId,
	    @NotNull
	    @Positive
	    Integer quantidade,
	    List<Long> acompanhamentosid,
	    List<Long> ingredientesid
	) {}