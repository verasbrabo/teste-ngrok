package app.DTO;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
		List<Boolean> diasTurno,
		Boolean emTurno,
		String role,
		String imagem
		) {
	
	public static FuncionarioOverviewDTO from(Funcionario f, Quiosque q) {
		LocalDateTime agora = LocalDateTime.now();
		
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
				f.getDiasTurnoLista(),
				estaEmTurno(f.getDiasTurnoLista(), f.getInicioTurno(), f.getFimTurno(), agora),
				f.getRole().toString(),
				f.getImagem() != null ? f.getImagem().getUrl() : null
				);
	}

	static Boolean estaEmTurno(
			List<Boolean> diasTurno,
			LocalTime inicio,
			LocalTime fim,
			LocalDateTime agora) {
		if (inicio == null || fim == null || diasTurno == null || diasTurno.size() != 7) {
			return null;
		}
		if (inicio.equals(fim)) {
			return false;
		}

		LocalTime horario = agora.toLocalTime();
		int diaAtual = agora.getDayOfWeek().getValue() % 7;
		if (inicio.isBefore(fim)) {
			return Boolean.TRUE.equals(diasTurno.get(diaAtual))
					&& !horario.isBefore(inicio)
					&& horario.isBefore(fim);
		}
		if (!horario.isBefore(inicio)) {
			return Boolean.TRUE.equals(diasTurno.get(diaAtual));
		}
		if (horario.isBefore(fim)) {
			return Boolean.TRUE.equals(diasTurno.get((diaAtual + 6) % 7));
		}
		return false;
	}

}
