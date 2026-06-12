package app.auth.DTO;

import app.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
		@NotBlank
		String nome,
		@NotBlank
		@Email
		String email,
		@NotBlank
		String password,
		// CPF é opcional: o cadastro de proprietário (quiosque) não o coleta.
		String cpf,
		@NotNull
		UserRole role,
		@NotBlank
		String telefone) {



}
