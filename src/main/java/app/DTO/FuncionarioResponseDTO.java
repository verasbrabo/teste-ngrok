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
		String email,
		UserRole role,
		String telefone,
		String imagem,
		LocalTime startShift,
		LocalTime finishShift,
		List<Boolean> diasAtivo
//		,LocalDate dataNasc
		) {

	public static FuncionarioResponseDTO from(Usuario u) {
		Funcionario funcionario = u instanceof Funcionario f ? f : null;
	    return new FuncionarioResponseDTO(
	    	u.getPublicId(),
				funcionario != null && funcionario.getQuiosque() != null
						? funcionario.getQuiosque().getId()
						: null,
	        u.getNome(),
	        funcionario != null ? funcionario.getEmail() : null,
	        u.getRole(),
	        u.getTelefone(),
	        u.getImagem() != null ? u.getImagem().getUrl() : null,
	        funcionario != null ? funcionario.getInicioTurno() : null,
	        funcionario != null ? funcionario.getFimTurno() : null,
	        funcionario != null ? funcionario.getDiasTurnoLista() : null
	    );
	}

}
