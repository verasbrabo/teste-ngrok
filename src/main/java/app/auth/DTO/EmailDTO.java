package app.auth.DTO;

import app.enums.TipoConta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record EmailDTO(
		@Email
		String email,
		@NotNull
		TipoConta tipoConta) {
	

}
