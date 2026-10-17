package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.QuiosqueUpdateDTO;
import app.DTO.QuiosqueViewDTO;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.UserRole;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class QuiosqueServiceTest {

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@Mock
	private Funcionario funcionario;

	@Mock
	private Usuario gerente;

	@Mock
	private Quiosque quiosque;

	@InjectMocks
	private QuiosqueService quiosqueService;

	@Test
	void deveReconsultarQuiosqueAntesDeMontarVisaoDoFuncionario() {
		Quiosque quiosqueVinculado = new Quiosque();
		quiosqueVinculado.setId(2L);
		quiosqueVinculado.setNome("Quiosque do funcionário");
		quiosqueVinculado.setOpeningTime(LocalTime.of(8, 0));
		quiosqueVinculado.setClosingTime(LocalTime.of(18, 0));
		quiosqueVinculado.setDistAtendimento(1800L);
		quiosqueVinculado.setCategorias(List.of());

		// A conta autenticada é de funcionário e possui um vínculo consultável.
		when(funcionario.getRole()).thenReturn(UserRole.FUNCIONARIO);
		when(quiosqueRepository.findByFuncionariosContaining(funcionario))
				.thenReturn(Optional.of(quiosqueVinculado));

		QuiosqueViewDTO resultado = quiosqueService.findByFuncionario(funcionario);

		assertEquals(2L, resultado.id());
		assertEquals("Quiosque do funcionário", resultado.nome());
		assertEquals(1800L, resultado.distAtendimento());
		verify(quiosqueRepository).findByFuncionariosContaining(funcionario);
		// A entidade do SecurityContext não deve fornecer o quiosque diretamente,
		// pois ela pode estar desconectada da sessão do Hibernate.
		verify(funcionario, never()).getQuiosque();
	}

	@Test
	void deveImpedirGerenteDeAlterarDiasDeFuncionamento() {
		// O gerente possui acesso ao quiosque, mas não é seu proprietário.
		QuiosqueUpdateDTO atualizacao = new QuiosqueUpdateDTO(
				null, null, null, null, null,
				List.of(false, true, true, true, true, true, false),
				null, null, null, null, null, null, null, null, null);

		when(quiosqueRepository.findById(7L)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(gerente)).thenReturn(true);
		when(quiosque.pertenceAoProprietario(gerente)).thenReturn(false);

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> quiosqueService.update(gerente, 7L, atualizacao));

		assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
		verify(quiosque, never()).atualizarDados(atualizacao);
	}

	@Test
	void deveLimitarBuscaDoClienteAUmQuilometro() {
		when(quiosqueRepository.findByDistanciaAtivo(
				anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
				anyDouble(), anyDouble())).thenReturn(List.of());

		quiosqueService.findByDistancia(-23.5505, -46.6333, 10000.0);

		// O terceiro argumento é o raio efetivamente aplicado pela API.
		verify(quiosqueRepository).findByDistanciaAtivo(
				eq(-23.5505), eq(-46.6333), eq(1000.0), anyDouble(),
				anyDouble(), anyDouble(), anyDouble());
	}

	@Test
	void deveNormalizarRaiosAntigosParaQuinhentosMetros() {
		quiosqueService.normalizarRaiosDeAtendimento();

		verify(quiosqueRepository).limitarDistanciaAtendimento(500L);
	}
}
