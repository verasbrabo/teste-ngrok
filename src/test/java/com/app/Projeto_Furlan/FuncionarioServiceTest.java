package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import app.DTO.FuncionarioResponseDTO;
import app.auth.AuthRepository;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.UserRole;
import app.repository.FuncionarioRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

	@Mock
	private AuthRepository usuarioRepository;

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@Mock
	private FuncionarioRepository funcionarioRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private PasswordEncoder passwordEncoder;

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
		when(funcionario.getEmail()).thenReturn("funcionario@teste.com");
		when(funcionario.getDiasTurnoLista()).thenReturn(
				List.of(false, true, true, true, true, true, false));

		List<FuncionarioResponseDTO> resultado = funcionarioService
				.getFuncionarios(proprietario, quiosqueSelecionadoId);

		assertEquals(1, resultado.size());
		assertEquals(funcionarioId, resultado.get(0).id());
		assertEquals(quiosqueSelecionadoId, resultado.get(0).quiosqueId());
		assertEquals("Funcionario do quiosque 22", resultado.get(0).nome());
		assertEquals("funcionario@teste.com", resultado.get(0).email());
		assertEquals(List.of(false, true, true, true, true, true, false),
				resultado.get(0).diasAtivo());
		verify(usuarioRepository).findByQuiosqueId(quiosqueSelecionadoId);
	}

	@Test
	void deveOcultarProprioGerenteDaListagem() {
		Long quiosqueId = 22L;
		UUID gerenteId = UUID.randomUUID();
		UUID funcionarioId = UUID.randomUUID();
		Funcionario gerente = mock(Funcionario.class);
		Funcionario funcionario = mock(Funcionario.class);
		Quiosque quiosque = mock(Quiosque.class);

		when(gerente.getRole()).thenReturn(UserRole.GERENTE);
		when(gerente.getPublicId()).thenReturn(gerenteId);
		when(quiosqueRepository.findById(quiosqueId)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(gerente)).thenReturn(true);
		when(quiosque.getId()).thenReturn(quiosqueId);
		when(usuarioRepository.findByQuiosqueId(quiosqueId))
				.thenReturn(List.of(gerente, funcionario));
		when(funcionario.getPublicId()).thenReturn(funcionarioId);
		when(funcionario.getQuiosque()).thenReturn(quiosque);
		when(funcionario.getNome()).thenReturn("Funcionario visivel");

		List<FuncionarioResponseDTO> resultado = funcionarioService
				.getFuncionarios(gerente, quiosqueId);

		assertEquals(1, resultado.size());
		assertEquals(funcionarioId, resultado.get(0).id());
	}

	@Test
	void deveExigirTrocaDepoisDeRedefinirSenhaDoFuncionario() {
		Long quiosqueId = 22L;
		UUID funcionarioId = UUID.randomUUID();
		Usuario proprietario = mock(Usuario.class);
		Quiosque quiosque = mock(Quiosque.class);
		Funcionario funcionario = mock(Funcionario.class);

		when(quiosqueRepository.findById(quiosqueId)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(proprietario)).thenReturn(true);
		when(usuarioRepository.findByQuiosqueAndPublicId(quiosque, funcionarioId))
				.thenReturn(Optional.of(funcionario));
		when(funcionario.getEmail()).thenReturn("funcionario@teste.com");
		when(funcionario.getLogin()).thenReturn("QUI01funcionario");
		when(passwordEncoder.encode(anyString())).thenReturn("hash-novo");

		funcionarioService.resetPassword(
				proprietario, quiosqueId, funcionarioId);

		verify(funcionario).setSenhaHash("hash-novo");
		verify(funcionario).setSenhaCifrada(null);
		verify(funcionario).setDeveTrocarSenha(true);
		verify(emailService).enviarCredenciaisFuncionario(
				org.mockito.ArgumentMatchers.eq("funcionario@teste.com"),
				org.mockito.ArgumentMatchers.eq("QUI01funcionario"),
				anyString());
	}
}
