package app.DTO;

import java.math.BigDecimal;

import app.entity.Acompanhamento;

public record AcompanhamentoViewDTO(
		Long id,
		String nome,
		BigDecimal valor) {
	
	public static AcompanhamentoViewDTO from(Acompanhamento a) {
		return new AcompanhamentoViewDTO(
				a.getId(),
				a.getNome(),
				a.getValor());
	}

}
