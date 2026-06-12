package app.DTO;

import java.time.LocalTime;

/**
 * Corpo de atualização parcial do quiosque (`PUT /quiosques/me`).
 *
 * Diferente de {@link QuiosqueDTO} (usado na criação, com todos os campos
 * obrigatórios), aqui todos os campos são opcionais: apenas os não-nulos são
 * aplicados (ver {@code Quiosque.atualizarDados}). Permite ao app persistir uma
 * edição isolada (ex.: só o nome ou só o horário) sem conhecer os demais
 * campos, que o `QuiosqueViewDTO` não expõe.
 */
public record QuiosqueUpdateDTO(
		String nome,
		String email,
		String cnpj,
		LocalTime openingTime,
		LocalTime closingTime,
		Long distAtendimento,
		String cep,
		String uf,
		String cidade,
		Double latitude,
		Double longitude
	) {}
