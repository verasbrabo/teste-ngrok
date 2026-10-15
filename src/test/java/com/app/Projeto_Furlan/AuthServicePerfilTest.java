package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

@ExtendWith(MockitoExtension.class)
class AuthServicePerfilTest {

	@Mock
	private AuthRepository usuarioRepository;

	@InjectMocks
	private AuthService authService;

	@Test
	void deveAlterarContatoSemTrocarLoginDoFuncionario() {
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
		verify(usuarioRepository, never()).existsByLoginAndRoleInAndIdNot(
				anyString(), anyCollection(), eq(funcionario.getId()));
		verify(usuarioRepository).save(funcionario);
	}
}
