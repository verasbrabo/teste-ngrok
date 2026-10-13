package app.auth.DTO;

import app.enums.TipoConta;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordDTO(
		String password,
		@NotNull TipoConta tipoConta) {

}
