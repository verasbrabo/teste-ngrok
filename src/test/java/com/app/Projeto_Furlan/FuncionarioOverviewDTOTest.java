package app.DTO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class FuncionarioOverviewDTOTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void deveIdentificarFuncionarioEmTurnoNormal() {
		List<Boolean> dias = diasComSegundaAtiva();

		assertTrue(FuncionarioOverviewDTO.estaEmTurno(
				dias, LocalTime.of(8, 0), LocalTime.of(18, 0),
				LocalDateTime.of(2026, 10, 12, 12, 0)));
		assertFalse(FuncionarioOverviewDTO.estaEmTurno(
				dias, LocalTime.of(8, 0), LocalTime.of(18, 0),
				LocalDateTime.of(2026, 10, 12, 18, 0)));
	}

	@Test
	void deveConsiderarMadrugadaComoParteDoTurnoAnterior() {
		List<Boolean> dias = diasComSegundaAtiva();

		assertTrue(FuncionarioOverviewDTO.estaEmTurno(
				dias, LocalTime.of(18, 0), LocalTime.of(2, 0),
				LocalDateTime.of(2026, 10, 13, 1, 0)));
		assertFalse(FuncionarioOverviewDTO.estaEmTurno(
				dias, LocalTime.of(18, 0), LocalTime.of(2, 0),
				LocalDateTime.of(2026, 10, 13, 3, 0)));
	}

	@Test
	void deveValidarEmailEQuantidadeDeDiasDoCadastro() {
		RegisterFuncionarioDTO dto = new RegisterFuncionarioDTO(
				"Funcionario", "email-invalido", null, null,
				LocalTime.of(8, 0), LocalTime.of(18, 0),
				List.of(true, true), app.enums.UserRole.FUNCIONARIO, "11999999999");

		assertFalse(validator.validateProperty(dto, "email").isEmpty());
		assertFalse(validator.validateProperty(dto, "diasTurno").isEmpty());
	}

	private List<Boolean> diasComSegundaAtiva() {
		return List.of(false, true, false, false, false, false, false);
	}
}
