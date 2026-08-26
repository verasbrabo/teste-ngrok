package app.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import app.entity.Cupom;
import app.enums.TipoCupom;

public record CupomCreateDTO(	
	
		 Long id,
		
		 TipoCupom tipoCupom,
		
		 BigDecimal valorDesconto,

	     BigDecimal valorMinimo,

	     BigDecimal descontoMaximo,
	    
	     LocalDateTime dataExpiracao,
	     
	     Boolean ativo
		) {

	public static CupomCreateDTO from(Cupom c) {
		return new CupomCreateDTO(
				c.getId(),
				c.getTipoCupom(),
				c.getValorDesconto(),
				c.getValorMinimo(),
				c.getDescontoMaximo(),
				c.getDataExpiracao(),
				c.isAtivo()
				);
	}
	
}
