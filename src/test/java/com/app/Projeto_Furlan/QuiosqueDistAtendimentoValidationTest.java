package app.DTO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class QuiosqueDistAtendimentoValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void deveAceitarRaioDeAteDoisMilMetros() {
		QuiosqueUpdateDTO dto = criarAtualizacao(2000L);

		assertTrue(validator.validateProperty(dto, "distAtendimento").isEmpty());
	}

	@Test
	void deveRejeitarRaioAcimaDeDoisMilMetros() {
		QuiosqueUpdateDTO dto = criarAtualizacao(2001L);

		assertFalse(validator.validateProperty(dto, "distAtendimento").isEmpty());
	}

	@Test
	void deveRejeitarRaioAcimaDoLimiteNoCadastro() {
		QuiosqueDTO dto = new QuiosqueDTO(
				"Quiosque Central", "central@example.com",
				LocalTime.of(8, 0), LocalTime.of(18, 0), null, null, 2001L,
				"01001000", "SP", "São Paulo", null, null, null,
				-23.5505, -46.6333);

		assertFalse(validator.validateProperty(dto, "distAtendimento").isEmpty());
	}

	@Test
	void deveRejeitarCalendarioComQuantidadeDiferenteDeSeteDias() {
		QuiosqueUpdateDTO dto = new QuiosqueUpdateDTO(
				null, null, null, null, null,
				List.of(true, true, true), null,
				null, null, null, null, null, null, null, null);

		assertFalse(validator.validateProperty(dto, "diasAtivo").isEmpty());
	}

	@Test
	void deveRejeitarRaioMenorQueUmMetro() {
		QuiosqueUpdateDTO dto = criarAtualizacao(0L);

		assertFalse(validator.validateProperty(dto, "distAtendimento").isEmpty());
	}

	private QuiosqueUpdateDTO criarAtualizacao(Long raio) {
		return new QuiosqueUpdateDTO(
				null, null, null, null, null, null, raio,
				null, null, null, null, null, null, null, null);
	}
}
