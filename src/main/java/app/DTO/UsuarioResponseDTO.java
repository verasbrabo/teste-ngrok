package app.DTO;

import java.time.LocalDate;
import java.util.UUID;

import app.entity.Usuario;

public record UsuarioResponseDTO(
		UUID id,
		String nome,
		String email,
		String telefone,
		LocalDate dataCadastro,
		String imagem,
		String role
		)
{
	public static UsuarioResponseDTO from(Usuario u) {
		return new UsuarioResponseDTO(
				u.getPublicId(),
				u.getNome(),
				u.getEmail(),
				u.getTelefone(),
				u.getDataCadastro(),
				u.getImagem() != null ? u.getImagem().getUrl() : null,
				u.getRole().getRole()
				);
	}
	
}
