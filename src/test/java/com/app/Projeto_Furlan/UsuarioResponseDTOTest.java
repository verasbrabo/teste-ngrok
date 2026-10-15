package app.DTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.entity.Funcionario;
import app.enums.UserRole;

class UsuarioResponseDTOTest {

	@Test
	void deveSepararEmailDeContatoDoLoginDoFuncionario() {
		Funcionario funcionario = new Funcionario(
				"Funcionario", "QUI01funcionario", "hash", "", "11999999999",
				UserRole.FUNCIONARIO);
		funcionario.setEmail("funcionario@teste.com");

		UsuarioResponseDTO dto = UsuarioResponseDTO.from(funcionario);

		assertEquals("funcionario@teste.com", dto.email());
		assertEquals("QUI01funcionario", dto.login());
	}

	@Test
	void deveUsarLoginComoEmailParaContaLegadaSemContatoSeparado() {
		Funcionario proprietario = new Funcionario(
				"Proprietario", "dono@teste.com", "hash", "", "11999999999",
				UserRole.PROPRIETARIO);

		UsuarioResponseDTO dto = UsuarioResponseDTO.from(proprietario);

		assertEquals("dono@teste.com", dto.email());
		assertEquals("dono@teste.com", dto.login());
	}
}
