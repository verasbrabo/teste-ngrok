package app.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class QuiosqueFuncionamentoTest {

	@Test
	void deveManterQuiosquesAntigosAtivosEmTodosOsDias() {
		Quiosque quiosque = new Quiosque();
		quiosque.setDiasAtivoBitmask(null);

		assertTrue(quiosque.getDiasAtivo().stream().allMatch(Boolean::booleanValue));
		assertTrue(quiosque.funcionaEm(LocalDate.of(2026, 10, 12)));
	}

	@Test
	void deveRespeitarDiaEHorarioDeFuncionamento() {
		Quiosque quiosque = criarQuiosqueDeSegunda(LocalTime.of(8, 0), LocalTime.of(18, 0));

		assertTrue(quiosque.estaAbertoEm(LocalDateTime.of(2026, 10, 12, 12, 0)));
		assertFalse(quiosque.estaAbertoEm(LocalDateTime.of(2026, 10, 12, 18, 0)));
		assertFalse(quiosque.estaAbertoEm(LocalDateTime.of(2026, 10, 13, 12, 0)));
	}

	@Test
	void deveConsiderarMadrugadaComoParteDoExpedienteAnterior() {
		Quiosque quiosque = criarQuiosqueDeSegunda(LocalTime.of(18, 0), LocalTime.of(2, 0));

		assertTrue(quiosque.estaAbertoEm(LocalDateTime.of(2026, 10, 12, 22, 0)));
		assertTrue(quiosque.estaAbertoEm(LocalDateTime.of(2026, 10, 13, 1, 0)));
		assertFalse(quiosque.estaAbertoEm(LocalDateTime.of(2026, 10, 13, 3, 0)));
	}

	private Quiosque criarQuiosqueDeSegunda(LocalTime abertura, LocalTime fechamento) {
		Quiosque quiosque = new Quiosque();
		quiosque.setOpeningTime(abertura);
		quiosque.setClosingTime(fechamento);
		List<Boolean> dias = new ArrayList<>(List.of(
				false, false, false, false, false, false, false));
		dias.set(1, true);
		quiosque.setDiasAtivo(dias);
		return quiosque;
	}
}
