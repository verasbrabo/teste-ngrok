package app.auth.DTO;

import app.enums.TipoConta;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDTO(
		@NotBlank
		String password,
		TipoConta tipoConta) {

}
