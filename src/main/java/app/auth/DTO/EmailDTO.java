package app.auth.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

import app.enums.TipoConta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDTO(
		@JsonAlias("login")
		@NotBlank
		@Email
		String email,
		TipoConta tipoConta) {
	

}
