package app.DTO;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import app.entity.Funcionario;
import app.entity.Quiosque;
import app.enums.UserRole;

public record FuncionarioOverviewDTO(
		UUID id,
		String nome,
		Long quiosque_id,
		String quiosque_nome,
		String openingTime,
		String closingTime,
		Boolean emTurno,
		String role,
		String imagem
		) {
	
	public static FuncionarioOverviewDTO from(Funcionario f, Quiosque q) {
		LocalTime agora = LocalTime.now();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		
		String inicio = f.getInicioTurno() != null ? f.getInicioTurno().format(formatter) : null;
		String fim = f.getFimTurno() != null ?f.getFimTurno().format(formatter) : null;
		
		return new FuncionarioOverviewDTO(
				f.getPublicId(),
				f.getNome(),
				q.getId(),
				q.getNome(),
				inicio,
				fim,
				inicio != null && fim != null ? agora.isAfter(f.getInicioTurno()) && agora.isBefore(f.getFimTurno()) ? true : false : null,
				f.getRole().toString(),
				f.getImagem() != null ? f.getImagem().getUrl() : null
				);
	}
	
	

}
