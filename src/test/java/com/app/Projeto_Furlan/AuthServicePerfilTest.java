package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.DTO.UpdateMeDTO;
import app.DTO.UsuarioResponseDTO;
import app.entity.Funcionario;
import app.enums.UserRole;
import app.repository.FuncionarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServicePerfilTest {

	@Mock
	private AuthRepository usuarioRepository;
	@Mock
	private FuncionarioRepository funcionarioRepository;

	@InjectMocks
	private AuthService authService;

	@Test
	void deveAlterarEmailSemAlterarLoginDoFuncionario() {
		Funcionario funcionario = new Funcionario(
				"Funcionario", "QUI01funcionario", "hash", "", "11999999999",
				UserRole.FUNCIONARIO);
		funcionario.setEmail("antigo@teste.com");

		UsuarioResponseDTO resposta = authService.putUsuario(
				funcionario, new UpdateMeDTO(null, "novo@teste.com", null));

		assertEquals("QUI01funcionario", funcionario.getLogin());
		assertEquals("novo@teste.com", funcionario.getEmail());
		assertEquals("novo@teste.com", resposta.email());
		assertEquals("QUI01funcionario", resposta.login());
		verify(funcionarioRepository).existsByEmailIgnoreCaseAndRoleIn(
				anyString(), anyCollection());
		verify(usuarioRepository).existsByLoginAndRoleIn(
				anyString(), anyCollection());
		verify(usuarioRepository).save(funcionario);
	}
}
