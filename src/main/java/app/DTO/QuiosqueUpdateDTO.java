package app.DTO;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Corpo de atualização parcial do quiosque (`PUT /quiosques/me`).
 *
 * Diferente de {@link QuiosqueDTO} (usado na criação, com todos os campos
 * obrigatórios), aqui todos os campos são opcionais: apenas os não-nulos são
 * aplicados (ver {@code Quiosque.atualizarDados}). Permite que app e site
 * persistam uma edição isolada sem reenviar os demais campos.
 */
public record QuiosqueUpdateDTO(
		String nome,
		String email,
		String cnpj,
		LocalTime openingTime,
		LocalTime closingTime,
		@Size(min = 7, max = 7, message = "Os dias de funcionamento devem conter os sete dias da semana")
		List<Boolean> diasAtivo,
		@Min(value = 1, message = "O raio de atendimento deve ser de pelo menos 1 metro")
		@Max(value = 500, message = "O raio de atendimento deve ser de no máximo 500 metros")
		Long distAtendimento,
		String cep,
		String uf,
		String cidade,
		String referencia,
		String rua,
		String numero,
		Double latitude,
		Double longitude
	) {}
