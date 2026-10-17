package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.DTO.TrocarSenhaDTO;
import app.entity.Funcionario;
import app.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class AuthServiceSenhaTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@Test
	void deveDesativarTrocaObrigatoriaDepoisDeDefinirSenha() {
		Funcionario funcionario = org.mockito.Mockito.mock(Funcionario.class);
		when(funcionario.getSenhaHash()).thenReturn("hash-atual");
		when(passwordEncoder.matches("senha-atual", "hash-atual")).thenReturn(true);
		when(passwordEncoder.encode("senha-nova")).thenReturn("hash-novo");

		authService.patchSenha(
				funcionario, new TrocarSenhaDTO("senha-atual", "senha-nova"));

		verify(funcionario).setSenhaHash("hash-novo");
		verify(funcionario).setDeveTrocarSenha(false);
	}

	@Test
	void devePermitirTrocaDeSenhaParaUsuarioAutenticado() throws NoSuchMethodException {
		// Garante que o primeiro acesso não volte a usar uma expressão inválida do Spring Security.
		var metodo = AuthController.class.getDeclaredMethod(
				"patchSenha", Usuario.class, TrocarSenhaDTO.class);
		PreAuthorize autorizacao = metodo.getAnnotation(PreAuthorize.class);

		assertEquals("isAuthenticated()", autorizacao.value());
	}
}
