package app.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuthServiceCpfTest {

	@Test
	void deveAceitarCpfAusenteNoCadastroDeFuncionario() {
		assertEquals("", AuthService.normalizarCpf(null));
		assertEquals("", AuthService.normalizarCpf(""));
	}

	@Test
	void deveNormalizarCpfValido() {
		assertEquals("52998224725", AuthService.normalizarCpf("529.982.247-25"));
	}

	@Test
	void deveDescartarCpfInvalido() {
		assertEquals("", AuthService.normalizarCpf("111.111.111-11"));
	}
}
