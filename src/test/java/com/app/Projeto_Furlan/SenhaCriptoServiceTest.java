package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SenhaCriptoServiceTest {

	private final SenhaCriptoService service =
			new SenhaCriptoService("chave-de-teste-segura", "5c0744940b5c369b");

	@Test
	void deveCifrarEDecifrarSenha() {
		String cifrado = service.cifrar("Senha@123");

		assertNotEquals("Senha@123", cifrado);
		assertEquals("Senha@123", service.decifrar(cifrado));
	}

	@Test
	void devePreservarAusenciaDeSenha() {
		assertNull(service.cifrar(null));
		assertNull(service.decifrar(null));
		assertNull(service.decifrar("  "));
	}
}
