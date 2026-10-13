package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.ItemCreateRequest;
import app.entity.Categoria;
import app.entity.Item;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.repository.AcompanhamentoRepository;
import app.repository.CategoriaRepository;
import app.repository.ItemRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@Mock
	private QuiosqueRepository quiosqueRepository;
	@Mock
	private CategoriaRepository categoriaRepository;
	@Mock
	private ItemRepository itemRepository;
	@Mock
	private AcompanhamentoRepository acompanhamentoRepository;
	@Mock
	private ImageService imageService;
	@InjectMocks
	private ItemService itemService;

	@Test
	void deveInformarItemInexistente() {
		when(itemRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseStatusException erro = assertThrows(ResponseStatusException.class,
				() -> itemService.findById(99L));

		assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
	}

	@Test
	void deveRejeitarAcompanhamentoDeOutroQuiosque() {
		Usuario usuario = mock(Usuario.class);
		Quiosque quiosque = mock(Quiosque.class);
		Categoria categoria = new Categoria();
		when(quiosqueRepository.findById(1L)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(usuario)).thenReturn(true);
		when(categoriaRepository.findByQuiosqueAndId(quiosque, 2L))
				.thenReturn(Optional.of(categoria));
		when(acompanhamentoRepository.findByIdInAndQuiosque(List.of(8L), quiosque))
				.thenReturn(List.of());
		ItemCreateRequest dados = new ItemCreateRequest(
				null, "Lanche", null, "Descricao", List.of("Pao"), List.of(8L),
				new BigDecimal("20.00"), null, 1L);

		ResponseStatusException erro = assertThrows(ResponseStatusException.class,
				() -> itemService.save(usuario, 1L, 2L, dados));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
		verify(itemRepository, never()).save(org.mockito.ArgumentMatchers.any(Item.class));
	}
}
