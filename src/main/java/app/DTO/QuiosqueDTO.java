package app.DTO;

import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuiosqueDTO(
		 @NotNull
		String nome,
		 @NotNull
		String email,
		
	    LocalTime openingTime,
	    
	    LocalTime closingTime,
	    
	    String cnpj,
	    
	    Long distAtendimento,
	    @NotNull
		String cep,
		 @NotNull
		String uf,
		 @NotNull
		String cidade,	
		String referencia,
		String rua,
		String numero,
		 @NotNull
		Double latitude,
		 @NotNull
		Double longitude
		) {

}
