package app.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.DTO.TrocarSenhaDTO;
import app.entity.Funcionario;

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
}
