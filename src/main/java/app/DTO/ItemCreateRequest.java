package app.DTO;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ItemCreateRequest(
		Long id,
		String nome,
		String tipo,
		String descricao,
		@NotNull
		List<IngredienteDTO> ingredientes,
		List<Long> acompanhamentoIds,
		BigDecimal valorBase,
		BigDecimal valorPromo,
		Long ordem) {
}
