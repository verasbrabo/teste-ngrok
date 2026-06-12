package app.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AvaliarDTO(
		@Min(1)
		@Max(5)
		int nota) {

}
