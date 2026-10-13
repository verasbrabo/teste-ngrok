package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.entity.Usuario;

class ImageServiceTest {

	private final ImageService imageService = new ImageService();

	@Test
	void deveRejeitarArquivoVazio() {
		var arquivo = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);

		ResponseStatusException erro = assertThrows(ResponseStatusException.class,
				() -> imageService.uploadImagemUsuario(mock(Usuario.class), arquivo));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
	}

	@Test
	void deveRejeitarArquivoQueNaoSejaImagem() {
		var arquivo = new MockMultipartFile(
				"file", "arquivo.txt", "text/plain", "conteudo".getBytes());

		ResponseStatusException erro = assertThrows(ResponseStatusException.class,
				() -> imageService.uploadImagemUsuario(mock(Usuario.class), arquivo));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
	}
}
