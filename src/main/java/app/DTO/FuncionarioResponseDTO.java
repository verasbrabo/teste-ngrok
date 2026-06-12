package app.DTO;

import java.util.UUID;

import app.entity.Usuario;
import app.enums.UserRole;

public record FuncionarioResponseDTO(
		UUID id,
		String nome,
		String email,
		UserRole role,
		String telefone,
		String imagem,
		// Senha do funcionário em texto puro, decifrada (AES) para reexibição na
		// gestão. Nula quando não há senha cifrada (ex.: contas antigas).
		String senha
//		,LocalDate dataNasc
		) {

	/**
	 * Monta o DTO com a [senha] já decifrada (resolvida na camada de serviço,
	 * que tem acesso ao {@code SenhaCriptoService}).
	 */
	public static FuncionarioResponseDTO from(Usuario u, String senha) {
	    return new FuncionarioResponseDTO(
	    	u.getPublicId(),
	        u.getNome(),
	        u.getEmail(),
	        u.getRole(),
	        u.getTelefone(),
	        u.getImagem() != null ? u.getImagem().getUrl() : null,
	        senha
	    );
	}

}
