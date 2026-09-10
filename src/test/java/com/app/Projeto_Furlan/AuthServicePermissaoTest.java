package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.enums.UserRole;

class AuthServicePermissaoTest {

	@Test
	void devePermitirProprietarioCadastrarGerente() {
		assertEquals(
				UserRole.GERENTE,
				AuthService.validarCargoCadastro(UserRole.PROPRIETARIO, UserRole.GERENTE));
	}

	@Test
	void deveImpedirGerenteDeCadastrarOutroGerente() {
		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> AuthService.validarCargoCadastro(UserRole.GERENTE, UserRole.GERENTE));

		assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
	}

	@Test
	void devePermitirGerenteCadastrarFuncionario() {
		assertEquals(
				UserRole.FUNCIONARIO,
				AuthService.validarCargoCadastro(UserRole.GERENTE, UserRole.FUNCIONARIO));
	}

	@Test
	void deveRejeitarCargoForaDaGestaoDeFuncionarios() {
		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> AuthService.validarCargoCadastro(
						UserRole.PROPRIETARIO, UserRole.PROPRIETARIO));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
	}
}
