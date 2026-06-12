package app.auth.DTO;

import java.util.UUID;

import app.enums.UserRole;

public record RegisterAdminResponseDTO(
		UUID id,
		String quiosque,
		String senha,
		String nome,
		String login,
		UserRole role,
		String telefone) {

}
