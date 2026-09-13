package app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.CupomRequestDTO;
import app.entity.Cupom;
import app.entity.Cliente;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.TipoCupom;
import app.repository.CupomRepository;
import app.repository.QuiosqueRepository;

@ExtendWith(MockitoExtension.class)
class CupomServiceTest {

	@Mock
	private QuiosqueRepository quiosqueRepository;

	@Mock
	private CupomRepository cupomRepository;

	@InjectMocks
	private CupomService cupomService;

	@Test
	void deveAceitarLimitesPercentuaisDeCincoENoventaECinco() {
		assertDoesNotThrow(() -> CupomService.validarRegras(percentual("5")));
		assertDoesNotThrow(() -> CupomService.validarRegras(percentual("95")));
	}

	@Test
	void deveRejeitarPercentualAbaixoDeCinco() {
		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> CupomService.validarRegras(percentual("4.99")));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
	}

	@Test
	void deveRejeitarPercentualAcimaDeNoventaECinco() {
		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> CupomService.validarRegras(percentual("95.01")));

		assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
	}

	@Test
	void deveExigirProprietarioDoQuiosqueNoCadastro() {
		Long quiosqueId = 22L;
		Usuario gerente = mock(Usuario.class);
		Quiosque quiosque = mock(Quiosque.class);
		when(quiosqueRepository.findById(quiosqueId)).thenReturn(Optional.of(quiosque));
		when(quiosque.pertenceAoProprietario(gerente)).thenReturn(false);

		ResponseStatusException erro = assertThrows(
				ResponseStatusException.class,
				() -> cupomService.save(gerente, quiosqueId, percentual("10")));

		assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
		verifyNoInteractions(cupomRepository);
	}

	@Test
	void deveExcluirCupomLogicamenteParaPreservarHistorico() {
		Long quiosqueId = 22L;
		Long cupomId = 7L;
		Usuario proprietario = mock(Usuario.class);
		Quiosque quiosque = mock(Quiosque.class);
		Cupom cupom = new Cupom(
				TipoCupom.FIXO,
				quiosque,
				new BigDecimal("10"),
				new BigDecimal("50"),
				null,
				LocalDateTime.now().plusDays(1),
				true);

		when(quiosqueRepository.findById(quiosqueId)).thenReturn(Optional.of(quiosque));
		when(quiosque.pertenceAoProprietario(proprietario)).thenReturn(true);
		when(cupomRepository.findByIdAndQuiosqueIdAndExcluidoFalse(cupomId, quiosqueId))
				.thenReturn(Optional.of(cupom));

		cupomService.delete(proprietario, quiosqueId, cupomId);

		assertFalse(cupom.isAtivo());
		assertTrue(cupom.isExcluido());
		verify(cupomRepository).save(cupom);
	}

	@Test
	void deveDesativarCuponsExpiradosEmLote() {
		when(cupomRepository.desativarExpirados(any(LocalDateTime.class))).thenReturn(2);

		int quantidade = cupomService.desativarCuponsExpirados();

		assertEquals(2, quantidade);
		verify(cupomRepository).desativarExpirados(any(LocalDateTime.class));
	}

	@Test
	void deveConsultarSomenteCuponsAindaDisponiveisParaOCliente() {
		Long quiosqueId = 22L;
		Cliente cliente = mock(Cliente.class);
		when(cliente.getId()).thenReturn(9L);
		when(quiosqueRepository.existsById(quiosqueId)).thenReturn(true);
		when(cupomRepository.findDisponiveisParaCliente(
				eq(quiosqueId), eq(9L), any(LocalDateTime.class)))
				.thenReturn(List.of());

		assertTrue(cupomService.findDisponiveisByQuiosque(cliente, quiosqueId).isEmpty());

		verify(cupomRepository).findDisponiveisParaCliente(
				eq(quiosqueId), eq(9L), any(LocalDateTime.class));
	}

	@Test
	void deveCalcularPercentualSemLimiteMonetario() {
		Cupom cupom = new Cupom(
				TipoCupom.PERCENTUAL,
				null,
				BigDecimal.TEN,
				BigDecimal.valueOf(50),
				null,
				LocalDateTime.now().plusDays(1),
				true);

		assertEquals(0, BigDecimal.valueOf(20).compareTo(
				cupom.calcularDesconto(BigDecimal.valueOf(200))));
	}

	/** Cria um contrato percentual válido, variando apenas o percentual testado. */
	private CupomRequestDTO percentual(String valor) {
		return new CupomRequestDTO(
				TipoCupom.PERCENTUAL,
				new BigDecimal(valor),
				BigDecimal.valueOf(50),
				null,
				LocalDateTime.now().plusDays(1));
	}
}
