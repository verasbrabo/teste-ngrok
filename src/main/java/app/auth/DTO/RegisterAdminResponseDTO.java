package app.auth.DTO;

import java.util.List;
import java.util.UUID;

import app.enums.UserRole;

public record RegisterAdminResponseDTO(
		UUID id,
		String quiosque,
		String senha,
		String email,
		String nome,
		String login,
		List<Boolean> diasTurno,
		UserRole role,
		String telefone) {

}
