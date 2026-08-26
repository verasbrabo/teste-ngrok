package app.DTO;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AvaliarDTO(
		@Min(1)
		@Max(5)
		int nota,
		@Length(max = 500, min = 1)
		@NotBlank
		String comentario
		) {

}
