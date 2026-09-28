package app.DTO;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AvaliarDTO(
		@Min(1)
		@Max(5)
		int nota,
		@Length(max = 500)
		String comentario
		) {

}
