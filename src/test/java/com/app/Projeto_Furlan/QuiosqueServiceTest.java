package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import app.DTO.QuiosqueViewDTO;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.enums.UserRole;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class QuiosqueServiceTest {

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@Mock
	private Funcionario funcionario;

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
}
