package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import app.auth.DTO.EmailDTO;
import app.DTO.RegisterFuncionarioDTO;
import app.auth.DTO.RegisterDTO;
import app.auth.DTO.ResetPasswordDTO;
import app.entity.Cliente;
import app.entity.Funcionario;
import app.entity.RecoveryToken;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.TipoConta;
import app.enums.UserRole;
import app.repository.FuncionarioRepository;
import app.repository.QuiosqueRepository;
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
	private FuncionarioRepository funcionarioRepository;
	@Mock
	private QuiosqueRepository quiosqueRepository;
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
				org.mockito.ArgumentMatchers.eq("QUI01joaosilva"), anyCollection()))
				.thenReturn(Optional.of(proprietario));

		var resultado = authService.loadUserByUsername(
				AuthService.identificadorAutenticacao("QUI01joaosilva", TipoConta.QUIOSQUE));

		assertSame(proprietario, resultado);
	}

	@Test
	void devePermitirClienteComContatoJaUsadoSomenteNoQuiosque() {
		RegisterDTO dados = new RegisterDTO(
				"Cliente", "mesmo@email.com", "Senha@123", "52998224725",
				UserRole.CLIENTE, "11999999999", null);
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
	void deveGerarLoginEEnviarCredenciaisSemExpoLasAoProprietario() {
		Usuario proprietario = org.mockito.Mockito.mock(Usuario.class);
		Quiosque quiosque = org.mockito.Mockito.mock(Quiosque.class);
		RegisterFuncionarioDTO dados = new RegisterFuncionarioDTO(
				"Maria Silva", "maria@teste.com", null, null,
				LocalTime.of(8, 0), LocalTime.of(18, 0),
				List.of(false, true, true, true, true, true, false),
				UserRole.FUNCIONARIO, "11999999999");

		when(proprietario.getRole()).thenReturn(UserRole.PROPRIETARIO);
		when(usuarioRepository.existsByTelefoneAndRoleIn(anyString(), anyCollection()))
				.thenReturn(false);
		when(quiosqueRepository.findById(22L)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(proprietario)).thenReturn(true);
		when(quiosque.getCodigo()).thenReturn("Q22");
		when(quiosque.getNome()).thenReturn("Quiosque 22");
		when(quiosque.getFuncionarios()).thenReturn(new ArrayList<>());
		when(usuarioRepository.findLoginByBase("Q22mariasilva"))
				.thenReturn(List.of());
		when(passwordEncoder.encode(anyString())).thenReturn("hash-provisorio");
		when(usuarioRepository.save(any(Funcionario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		var resposta = authService.registerFuncionario(proprietario, 22L, dados);

		ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
		verify(usuarioRepository).save(captor.capture());
		Funcionario salvo = captor.getValue();
		assertEquals("Q22mariasilva", salvo.getLogin());
		assertEquals("maria@teste.com", salvo.getEmail());
		assertNull(salvo.getSenhaCifrada());
		assertEquals("maria@teste.com", resposta.email());
		verify(emailService).enviarCredenciaisFuncionario(
				org.mockito.ArgumentMatchers.eq("maria@teste.com"),
				org.mockito.ArgumentMatchers.eq("Q22mariasilva"),
				anyString());
	}

	@Test
	void deveRejeitarEmailRepetidoNoMesmoTipoDeConta() {
		RegisterDTO dados = new RegisterDTO(
				"Cliente", "mesmo@email.com", "Senha@123", null,
				UserRole.CLIENTE, "11999999999", null);
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

	@Test
	void deveAceitarFormatoWebNaRecuperacaoDoProprietario() {
		Funcionario proprietario = org.mockito.Mockito.mock(Funcionario.class);
		when(usuarioRepository.findByLoginAndRole("dono@teste.com", UserRole.PROPRIETARIO))
				.thenReturn(Optional.of(proprietario));
		when(recoveryTokenRepository.existsByUsuarioAndEnviadoAfterAndExpiracaoAfter(
				org.mockito.ArgumentMatchers.eq(proprietario), any(), any())).thenReturn(false);

		authService.forgotPassword(new EmailDTO("dono@teste.com", null));

		verify(recoveryTokenRepository).save(any(RecoveryToken.class));
		verify(emailService).enviarEmail(
				org.mockito.ArgumentMatchers.eq("dono@teste.com"),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void naoDeveRecuperarSenhaDeFuncionarioPeloFluxoPublico() {
		authService.forgotPassword(new EmailDTO("funcionario@teste.com", TipoConta.QUIOSQUE));

		verify(usuarioRepository).findByLoginAndRole(
				"funcionario@teste.com", UserRole.PROPRIETARIO);
		verify(recoveryTokenRepository, never()).save(any());
		verify(emailService, never()).enviarEmail(any(), any(), any());
	}

	@Test
	void deveManterCadastroJsonDeProprietarioParaWeb() {
		RegisterDTO dados = new RegisterDTO(
				"Proprietario", "dono@teste.com", "Senha@123", null,
				UserRole.PROPRIETARIO, "11988888888", "12.345.678/0001-90");
		when(passwordEncoder.encode("Senha@123")).thenReturn("hash");
		when(usuarioRepository.save(any(Funcionario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		Usuario resultado = authService.register(dados);

		assertEquals(UserRole.PROPRIETARIO, resultado.getRole());
		assertEquals("12345678000190", ((Funcionario) resultado).getCnpj());
		verify(funcionarioRepository).existsByCnpj("12345678000190");
	}

	@Test
	void deveRedefinirSenhaDoProprietarioSemTipoNoFormatoWeb() {
		Funcionario proprietario = org.mockito.Mockito.mock(Funcionario.class);
		RecoveryToken token = new RecoveryToken(
				"AB12", proprietario, LocalDateTime.now().plusMinutes(30), LocalDateTime.now());
		when(recoveryTokenRepository.findByTokenAndUsuarioRoleInAndExpiracaoAfter(
				org.mockito.ArgumentMatchers.eq("AB12"),
				anyCollection(),
				any())).thenReturn(Optional.of(token));
		when(passwordEncoder.encode("SenhaNova@123")).thenReturn("hash-novo");

		authService.resetPassword(
				"AB12", new ResetPasswordDTO("SenhaNova@123", null));

		verify(proprietario).setSenhaHash("hash-novo");
		verify(proprietario).setSenhaCifrada(null);
		verify(proprietario).setDeveTrocarSenha(false);
		verify(usuarioRepository).save(proprietario);
		verify(recoveryTokenRepository).delete(token);
	}
}
