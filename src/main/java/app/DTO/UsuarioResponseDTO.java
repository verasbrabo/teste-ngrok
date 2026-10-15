package app.DTO;

import java.time.LocalDate;
import java.util.UUID;

import app.entity.Funcionario;
import app.entity.Usuario;

public record UsuarioResponseDTO(
		UUID id,
		String nome,
		String email,
		String login,
		String telefone,
		String cnpj,
		LocalDate dataCadastro,
		String imagem,
		String role
		)
{
	public static UsuarioResponseDTO from(Usuario u) {
		return new UsuarioResponseDTO(
				u.getPublicId(),
				u.getNome(),
				emailDeContato(u),
				u.getLogin(),
				u.getTelefone(),
				u instanceof Funcionario funcionario ? funcionario.getCnpj() : null,
				u.getDataCadastro(),
				u.getImagem() != null ? u.getImagem().getUrl() : null,
				u.getRole().getRole()
				);
	}

	private static String emailDeContato(Usuario usuario) {
		if (usuario instanceof Funcionario funcionario
				&& funcionario.getEmail() != null
				&& !funcionario.getEmail().isBlank()) {
			return funcionario.getEmail();
		}
		return usuario.getLogin();
	}
	
}
