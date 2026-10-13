package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import app.auth.DTO.EmailDTO;
import app.auth.DTO.RegisterDTO;
import app.entity.Cliente;
import app.entity.Funcionario;
import app.entity.RecoveryToken;
import app.enums.TipoConta;
import app.enums.UserRole;
import app.repository.RecoveryTokenRepository;
import app.service.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTipoContaTest {

	@Mock
	private AuthRepository usuarioRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private RecoveryTokenRepository recoveryTokenRepository;
	@Mock
	private EmailService emailService;
	@InjectMocks
	private AuthService authService;

	@Test
	void deveSelecionarContaClientePeloContextoDoLogin() {
		Cliente cliente = org.mockito.Mockito.mock(Cliente.class);
		when(usuarioRepository.findByLoginAndRole("mesmo@email.com", UserRole.CLIENTE))
				.thenReturn(Optional.of(cliente));

		var resultado = authService.loadUserByUsername(
				AuthService.identificadorAutenticacao("mesmo@email.com", TipoConta.CLIENTE));

		assertSame(cliente, resultado);
	}

	@Test
	void deveSelecionarContaDoQuiosquePeloContextoDoLogin() {
		Funcionario proprietario = org.mockito.Mockito.mock(Funcionario.class);
		when(usuarioRepository.findByLoginAndRoleIn(
				org.mockito.ArgumentMatchers.eq("mesmo@email.com"), anyCollection()))
				.thenReturn(Optional.of(proprietario));

		var resultado = authService.loadUserByUsername(
				AuthService.identificadorAutenticacao("mesmo@email.com", TipoConta.QUIOSQUE));

		assertSame(proprietario, resultado);
	}

	@Test
	void devePermitirClienteComContatoJaUsadoSomenteNoQuiosque() {
		RegisterDTO dados = new RegisterDTO(
				"Cliente", "mesmo@email.com", "Senha@123", "52998224725",
				UserRole.CLIENTE, "11999999999");
		when(passwordEncoder.encode("Senha@123")).thenReturn("hash");
		when(usuarioRepository.save(any(Cliente.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		var resultado = authService.register(dados);

		assertEquals(UserRole.CLIENTE, resultado.getRole());
		assertEquals("mesmo@email.com", resultado.getLogin());
		verify(usuarioRepository).existsByLoginAndRole("mesmo@email.com", UserRole.CLIENTE);
		verify(usuarioRepository).existsByTelefoneAndRole("11999999999", UserRole.CLIENTE);
	}

	@Test
	void deveRejeitarEmailRepetidoNoMesmoTipoDeConta() {
		RegisterDTO dados = new RegisterDTO(
				"Cliente", "mesmo@email.com", "Senha@123", null,
				UserRole.CLIENTE, "11999999999");
		when(usuarioRepository.existsByLoginAndRole("mesmo@email.com", UserRole.CLIENTE))
				.thenReturn(true);

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class, () -> authService.register(dados));

		assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void deveRecuperarSenhaDaContaClienteCorreta() {
		Cliente cliente = org.mockito.Mockito.mock(Cliente.class);
		when(usuarioRepository.findByLoginAndRole("mesmo@email.com", UserRole.CLIENTE))
				.thenReturn(Optional.of(cliente));
		when(recoveryTokenRepository.existsByUsuarioAndEnviadoAfterAndExpiracaoAfter(
				org.mockito.ArgumentMatchers.eq(cliente), any(), any())).thenReturn(false);

		authService.forgotPassword(new EmailDTO("mesmo@email.com", TipoConta.CLIENTE));

		verify(recoveryTokenRepository).save(any(RecoveryToken.class));
		verify(emailService).enviarEmail(
				org.mockito.ArgumentMatchers.eq("mesmo@email.com"),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString());
	}
}
