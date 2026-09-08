package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.DTO.FuncionarioResponseDTO;
import app.auth.AuthRepository;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

	@Mock
	private AuthRepository usuarioRepository;

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@Mock
	private SenhaCriptoService senhaCriptoService;

	@InjectMocks
	private FuncionarioService funcionarioService;

	@Test
	void deveListarSomenteFuncionariosDoQuiosqueInformado() {
		Long quiosqueSelecionadoId = 22L;
		Usuario proprietario = mock(Usuario.class);
		Quiosque quiosqueSelecionado = mock(Quiosque.class);
		Funcionario funcionario = mock(Funcionario.class);
		UUID funcionarioId = UUID.randomUUID();

		when(quiosqueRepository.findById(quiosqueSelecionadoId))
				.thenReturn(Optional.of(quiosqueSelecionado));
		when(quiosqueSelecionado.usuarioPossuiAcesso(proprietario)).thenReturn(true);
		when(quiosqueSelecionado.getId()).thenReturn(quiosqueSelecionadoId);
		when(usuarioRepository.findByQuiosqueId(quiosqueSelecionadoId))
				.thenReturn(List.of(funcionario));
		when(funcionario.getQuiosque()).thenReturn(quiosqueSelecionado);
		when(funcionario.getPublicId()).thenReturn(funcionarioId);
		when(funcionario.getNome()).thenReturn("Funcionario do quiosque 22");

		List<FuncionarioResponseDTO> resultado = funcionarioService
				.getFuncionarios(proprietario, quiosqueSelecionadoId);

		assertEquals(1, resultado.size());
		assertEquals(funcionarioId, resultado.get(0).id());
		assertEquals(quiosqueSelecionadoId, resultado.get(0).quiosqueId());
		assertEquals("Funcionario do quiosque 22", resultado.get(0).nome());
		verify(usuarioRepository).findByQuiosqueId(quiosqueSelecionadoId);
	}
}
