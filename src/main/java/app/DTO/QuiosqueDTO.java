package app.DTO;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuiosqueDTO(
		 @NotNull
		String nome,
		 @NotNull
		String email,
		
	    LocalTime openingTime,
	    
	    LocalTime closingTime,
	    @Size(min = 7, max = 7, message = "Os dias de funcionamento devem conter os sete dias da semana")
	    List<Boolean> diasAtivo,
	    String cnpj,
	    
	    @Min(value = 1, message = "O raio de atendimento deve ser de pelo menos 1 metro")
	    @Max(value = 2000, message = "O raio de atendimento deve ser de no máximo 2000 metros")
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
