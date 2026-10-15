package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.entity.Categoria;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.Categorias;
import app.repository.CategoriaRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

	@Mock
	private CategoriaRepository categoriaRepository;
	@Mock
	private QuiosqueRepository quiosqueRepository;
	@InjectMocks
	private CategoriaService categoriaService;

	@Test
	void deveListarSomenteCategoriasAtivas() {
		Quiosque quiosque = new Quiosque();
		Categoria ativa = new Categoria(Categorias.Lanches, 1);
		ativa.setId(10L);
		Categoria inativa = new Categoria(Categorias.Bebidas, 2);
		inativa.setAtivo(false);
		when(quiosqueRepository.findById(1L)).thenReturn(Optional.of(quiosque));
		when(categoriaRepository.findByQuiosqueOrderByOrdem(quiosque))
				.thenReturn(List.of(ativa, inativa));

		var resultado = categoriaService.findByQuiosque(1L);

		assertEquals(1, resultado.size());
		assertEquals("Lanches", resultado.get(0).nome());
	}

	@Test
	void deveListarComoDisponiveisAsCategoriasNaoUtilizadasOuInativas() {
		when(quiosqueRepository.existsById(1L)).thenReturn(true);
		when(categoriaRepository.buscarNomesAtivosPorQuiosqueId(1L))
				.thenReturn(List.of(Categorias.Lanches, Categorias.Bebidas));

		var resultado = categoriaService.findDisponiveisByQuiosque(1L);

		assertFalse(resultado.contains(Categorias.Lanches));
		assertFalse(resultado.contains(Categorias.Bebidas));
		assertTrue(resultado.contains(Categorias.Sobremesas));
	}

	@Test
	void deveFazerExclusaoLogicaELiberarOrdem() {
		Usuario usuario = org.mockito.Mockito.mock(Usuario.class);
		Quiosque quiosque = org.mockito.Mockito.mock(Quiosque.class);
		Categoria categoria = new Categoria(Categorias.Bebidas, 4);
		when(quiosqueRepository.findById(1L)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(usuario)).thenReturn(true);
		when(categoriaRepository.findByQuiosqueAndId(quiosque, 2L))
				.thenReturn(Optional.of(categoria));

		categoriaService.deleteById(usuario, 1L, 2L);

		assertFalse(categoria.isAtivo());
		assertNull(categoria.getOrdem());
		verify(categoriaRepository).save(categoria);
	}

	@Test
	void deveNegarAlteracaoSemAcessoAoQuiosque() {
		Usuario usuario = org.mockito.Mockito.mock(Usuario.class);
		Quiosque quiosque = org.mockito.Mockito.mock(Quiosque.class);
		when(quiosqueRepository.findById(1L)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(usuario)).thenReturn(false);

		ResponseStatusException erro = assertThrows(ResponseStatusException.class,
				() -> categoriaService.deleteById(usuario, 1L, 2L));

		assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
	}
}
