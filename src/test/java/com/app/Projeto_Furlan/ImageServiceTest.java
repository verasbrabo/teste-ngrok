package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.auth.AuthRepository;
import app.entity.Imagem;
import app.entity.Usuario;
import app.repository.ImageRepository;
import app.repository.ItemRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	private AuthRepository authRepository;
	@Mock
	private QuiosqueRepository quiosqueRepository;
	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ImageRepository imageRepository;
	@InjectMocks
	private ImageService imageService;

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

	@Test
	void deveDesvincularImagemAntesDeExcluirRegistro() {
		Usuario usuario = mock(Usuario.class);
		Imagem imagem = mock(Imagem.class);
		when(usuario.getImagem()).thenReturn(imagem);
		when(imagem.getUrl()).thenReturn("/uploads/arquivo-inexistente.png");

		imageService.deleteImagemUsuario(usuario);

		verify(usuario).setImagem(null);
		verify(authRepository).saveAndFlush(usuario);
		verify(imageRepository).delete(imagem);
	}
}
