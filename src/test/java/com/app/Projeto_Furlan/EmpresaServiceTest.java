package app.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.auth.AuthRepository;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.enums.Categorias;
import app.repository.FaturamentoPorQuiosqueProjection;
import app.repository.ItemPedidoRepository;
import app.repository.PedidosPorCategoriaProjection;
import app.repository.PedidoRepository;
import app.repository.ProdutoMaisVendidoProjection;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

	@Mock
	private QuiosqueRepository quiosqueRepository;
	@Mock
	private PedidoRepository pedidoRepository;
	@Mock
	private AuthRepository funcionarioRepository;
	@Mock
	private ItemPedidoRepository itemPedidoRepository;
	@InjectMocks
	private EmpresaService empresaService;

	@Test
	void deveRetornarOverviewVazioParaProprietarioSemQuiosque() {
		Funcionario proprietario = mock(Funcionario.class);
		when(quiosqueRepository.findByProprietario(proprietario)).thenReturn(List.of());

		var resultado = empresaService.overview(proprietario);

		assertTrue(resultado.quiosques().isEmpty());
		assertTrue(resultado.funcionarios().isEmpty());
		assertTrue(resultado.pedidos().isEmpty());
		verifyNoInteractions(pedidoRepository, funcionarioRepository, itemPedidoRepository);
	}

	@Test
	void deveRejeitarPeriodoDeEstatisticasInvertido() {
		Funcionario proprietario = mock(Funcionario.class);

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> empresaService.statistics(
						proprietario,
						LocalDate.of(2026, 10, 2),
						LocalDate.of(2026, 10, 1),
						null));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
		verifyNoInteractions(quiosqueRepository, pedidoRepository, itemPedidoRepository);
	}

	@Test
	void deveMontarEstatisticasCompativeisComOPainelWeb() {
		Funcionario proprietario = mock(Funcionario.class);
		Quiosque quiosque = mock(Quiosque.class);
		FaturamentoPorQuiosqueProjection faturamento = mock(FaturamentoPorQuiosqueProjection.class);
		ProdutoMaisVendidoProjection produto = mock(ProdutoMaisVendidoProjection.class);
		PedidosPorCategoriaProjection categoria = mock(PedidosPorCategoriaProjection.class);
		LocalDate inicio = LocalDate.of(2026, 10, 1);
		LocalDate fim = LocalDate.of(2026, 10, 2);

		when(proprietario.getId()).thenReturn(7L);
		when(quiosque.getId()).thenReturn(10L);
		when(quiosque.getNome()).thenReturn("Quiosque Centro");
		when(quiosqueRepository.findByProprietario(proprietario)).thenReturn(List.of(quiosque));

		when(faturamento.getKioskId()).thenReturn(10L);
		when(faturamento.getData()).thenReturn(inicio);
		when(faturamento.getFaturamento()).thenReturn(new BigDecimal("75.50"));
		when(pedidoRepository.buscarFaturamentoPorQuiosque(
				7L, null, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()))
				.thenReturn(List.of(faturamento));

		when(produto.getProductId()).thenReturn(20L);
		when(produto.getProductName()).thenReturn("Água de coco");
		when(produto.getUnitsSold()).thenReturn(3L);
		when(itemPedidoRepository.buscarProdutosMaisVendidos(
				org.mockito.ArgumentMatchers.eq(7L),
				org.mockito.ArgumentMatchers.isNull(),
				org.mockito.ArgumentMatchers.eq(inicio.atStartOfDay()),
				org.mockito.ArgumentMatchers.eq(fim.plusDays(1).atStartOfDay()),
				any(Pageable.class)))
				.thenReturn(List.of(produto));

		when(categoria.getCategoryId()).thenReturn(30L);
		when(categoria.getCategoryName()).thenReturn(Categorias.Bebidas);
		when(categoria.getOrderCount()).thenReturn(2L);
		when(itemPedidoRepository.buscarPedidosPorCategoria(
				7L, null, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()))
				.thenReturn(List.of(categoria));

		var resultado = empresaService.statistics(proprietario, inicio, fim, null);

		assertEquals(List.of(inicio, fim), resultado.revenueByKiosk().dates());
		assertEquals(
				List.of(new BigDecimal("75.50"), BigDecimal.ZERO),
				resultado.revenueByKiosk().kiosks().get(0).revenue());
		assertEquals("Água de coco", resultado.topProducts().products().get(0).productName());
		assertEquals("Bebidas", resultado.ordersByCategory().categories().get(0).categoryName());
		assertEquals(BigDecimal.ZERO, resultado.dailyRevenue().points().get(1).value());
	}
}
