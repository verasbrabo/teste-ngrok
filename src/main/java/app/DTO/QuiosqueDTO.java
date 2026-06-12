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
		 @NotNull
		String cnpj,
		@NotNull
	    LocalTime openingTime,
	    @NotNull
	    LocalTime closingTime,
	    @NotNull
	    Long distAtendimento,
	    @NotNull
		String cep,
		 @NotNull
		String uf,
		 @NotNull
		String cidade,
		 @NotNull
		Double latitude,
		 @NotNull
		Double longitude
		) {

}
