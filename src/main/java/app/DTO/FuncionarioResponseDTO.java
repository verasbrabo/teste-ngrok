package app.DTO;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import app.entity.Funcionario;
import app.entity.Usuario;
import app.enums.UserRole;

public record FuncionarioResponseDTO(
		UUID id,
		Long quiosqueId,
		String nome,
		String login,
		String email,
		UserRole role,
		String telefone,
		String imagem,
		LocalTime startShift,
		LocalTime finishShift,
		List<Boolean> diasAtivo,
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
		Funcionario funcionario = u instanceof Funcionario f ? f : null;
	    return new FuncionarioResponseDTO(
	    	u.getPublicId(),
				funcionario != null && funcionario.getQuiosque() != null
						? funcionario.getQuiosque().getId()
						: null,
	        u.getNome(),
	        u.getLogin(),
	        funcionario != null ? funcionario.getEmail() : null,
	        u.getRole(),
	        u.getTelefone(),
	        u.getImagem() != null ? u.getImagem().getUrl() : null,
	        funcionario != null ? funcionario.getInicioTurno() : null,
	        funcionario != null ? funcionario.getFimTurno() : null,
	        funcionario != null ? funcionario.getDiasTurnoLista() : null,
	        senha
	    );
	}

}
