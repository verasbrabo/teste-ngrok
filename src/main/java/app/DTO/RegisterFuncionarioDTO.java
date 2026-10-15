package app.DTO;

import java.time.LocalTime;
import java.util.List;

import app.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterFuncionarioDTO(
		@NotBlank
		String nome,
		@NotBlank
		@Email
		String email,
		// Campo legado aceito pelo app; a API continua gerando a senha provisória.
		String password,
		// CPF é opcional: o cadastro de proprietário (quiosque) não o coleta.
		String cpf,
		@NotNull
	    LocalTime startShift,
	    @NotNull
	    LocalTime finishShift,
	    @Size(min = 7, max = 7, message = "Os dias de trabalho devem conter os sete dias da semana")
	    List<Boolean> diasTurno,
		@NotNull
		UserRole role,
		@NotBlank
		String telefone
		
		) {

}
