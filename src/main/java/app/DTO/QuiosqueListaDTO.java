package app.DTO;

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
		String imagem) {

	public static QuiosqueListaDTO from(Quiosque quiosque) {
		return new QuiosqueListaDTO(
				quiosque.getId(),
				quiosque.getNome(),
				quiosque.getEmail(),
				quiosque.getEndereco() != null ? quiosque.getEndereco().getCidade() : null,
				quiosque.getEndereco() != null ? quiosque.getEndereco().getUf() : null,
				quiosque.getStatus(),
				quiosque.getImagem() != null ? quiosque.getImagem().getUrl() : null);
	}
}
