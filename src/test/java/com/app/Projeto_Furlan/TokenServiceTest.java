package app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;

import app.entity.Usuario;
import app.enums.UserRole;

class TokenServiceTest {

	private final TokenService tokenService = new TokenService();

	@BeforeEach
	void configurarSegredo() {
		ReflectionTestUtils.setField(tokenService, "secret", "segredo-unitario-token");
	}

	@Test
	void deveGerarEValidarTokenComPapelDoUsuario() {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getLogin()).thenReturn("funcionario01");
		when(usuario.getRole()).thenReturn(UserRole.FUNCIONARIO);
		when(usuario.getUltimoLogin()).thenReturn(null);

		String token = tokenService.generateToken(usuario);

		assertEquals("funcionario01", tokenService.validateToken(token));
		assertEquals(
				UserRole.FUNCIONARIO,
				tokenService.validateTokenPayload(token).role());
		assertEquals("FUNCIONARIO", JWT.decode(token).getClaim("role").asString());
		assertTrue(JWT.decode(token).getClaim("trocarSenha").asBoolean());
	}

	@Test
	void deveRejeitarTokenAdulterado() {
		assertEquals("", tokenService.validateToken("token.invalido.assinatura"));
	}
}
