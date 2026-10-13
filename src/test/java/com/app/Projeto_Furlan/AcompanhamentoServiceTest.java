package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.DTO.AcompanhamentoCreateDTO;
import app.entity.Acompanhamento;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.repository.AcompanhamentoRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class AcompanhamentoServiceTest {

	@Mock
	private QuiosqueRepository quiosqueRepository;
	@Mock
	private AcompanhamentoRepository acompanhamentoRepository;
	@InjectMocks
	private AcompanhamentoService acompanhamentoService;

	@Test
	void deveCadastrarAcompanhamentoNoQuiosqueAutorizado() {
		Usuario usuario = mock(Usuario.class);
		Quiosque quiosque = mock(Quiosque.class);
		when(quiosqueRepository.findById(7L)).thenReturn(Optional.of(quiosque));
		when(quiosque.usuarioPossuiAcesso(usuario)).thenReturn(true);
		when(acompanhamentoRepository.save(any(Acompanhamento.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		var resultado = acompanhamentoService.save(
				usuario, 7L, new AcompanhamentoCreateDTO("Molho", new BigDecimal("2.50")));

		assertEquals("Molho", resultado.nome());
		assertEquals(new BigDecimal("2.50"), resultado.valor());
		ArgumentCaptor<Acompanhamento> captor = ArgumentCaptor.forClass(Acompanhamento.class);
		verify(acompanhamentoRepository).save(captor.capture());
		assertEquals(quiosque, captor.getValue().getQuiosque());
	}
}
