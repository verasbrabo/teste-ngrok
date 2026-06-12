package app.DTO;

import java.math.BigDecimal;

public record AcompanhamentoCreateDTO(
		String nome,
		BigDecimal valor) {

}
