package app.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.auth.AuthRepository;
import app.entity.Funcionario;
import app.repository.ItemPedidoRepository;
import app.repository.PedidoRepository;
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
}
