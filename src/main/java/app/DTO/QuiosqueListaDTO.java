package app.DTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

import app.entity.Quiosque;
import app.enums.StatusConta;

/**
 * Resumo usado na seleção de quiosques do proprietário.
 */
public record QuiosqueListaDTO(
		Long id,
		String nome,
		String email,
		String cidade,
		String uf,
		StatusConta status,
		String imagem,
		String openingTime,
		String closingTime,
		List<Boolean> diasAtivo,
		boolean aberto) {

	public static QuiosqueListaDTO from(Quiosque quiosque) {
		// Mantém o horário no mesmo formato consumido pelos aplicativos Flutter.
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return new QuiosqueListaDTO(
				quiosque.getId(),
				quiosque.getNome(),
				quiosque.getEmail(),
				quiosque.getEndereco() != null ? quiosque.getEndereco().getCidade() : null,
				quiosque.getEndereco() != null ? quiosque.getEndereco().getUf() : null,
				quiosque.getStatus(),
				quiosque.getImagem() != null ? quiosque.getImagem().getUrl() : null,
				quiosque.getOpeningTime() != null
						? quiosque.getOpeningTime().format(formatter)
						: null,
				quiosque.getClosingTime() != null
						? quiosque.getClosingTime().format(formatter)
						: null,
				quiosque.getDiasAtivo(),
				quiosque.estaAberto());
	}
}
