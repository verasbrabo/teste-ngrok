package app.auth.DTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class AuthDtoCompatibilityTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void deveAceitarLoginComoAliasDeEmailNoFormatoWeb() throws Exception {
		EmailDTO dto = objectMapper.readValue(
				"{\"login\":\"dono@teste.com\"}", EmailDTO.class);

		assertEquals("dono@teste.com", dto.email());
		assertNull(dto.tipoConta());
	}

	@Test
	void deveAceitarResetSemTipoDeContaNoFormatoWeb() throws Exception {
		ResetPasswordDTO dto = objectMapper.readValue(
				"{\"password\":\"SenhaNova@123\"}", ResetPasswordDTO.class);

		assertEquals("SenhaNova@123", dto.password());
		assertNull(dto.tipoConta());
	}
}
