package app.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.DTO.LocalizacaoEntregaCreateDTO;
import app.DTO.PontoRastreamentoEntregaDTO;
import app.DTO.RastreamentoEntregaDTO;
import app.entity.Coordenada;
import app.entity.Pedido;
import app.entity.PontoRastreamentoEntrega;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusPedido;
import app.repository.PedidoRepository;
import app.repository.PontoRastreamentoEntregaRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

/**
 * Coordena a publicacao do GPS do entregador e o acompanhamento SSE do cliente.
 */
@Service
public class RastreamentoEntregaService {
	private static final Duration ATRASO_MAXIMO = Duration.ofMinutes(5);
	private static final Duration ADIANTAMENTO_MAXIMO = Duration.ofMinutes(1);
	private static final double PRECISAO_MAXIMA_METROS = 25.0;
	private static final double DISTANCIA_MINIMA_METROS = 5.0;
	private static final double DISTANCIA_MAXIMA_POR_PRECISAO_METROS = 10.0;
	private static final double FATOR_DISTANCIA_POR_PRECISAO = 0.4;
	private static final long INTERVALO_ESTACIONADO_SEGUNDOS = 15;
	private static final double VELOCIDADE_MAXIMA_METROS_SEGUNDO = 12.0;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private QuiosqueRepository quiosqueRepository;

	@Autowired
	private PontoRastreamentoEntregaRepository pontoRepository;

	// Cada pedido possui apenas os clientes que estao com a tela de tracking
	// aberta. CopyOnWriteArrayList permite emitir e remover conexoes em paralelo.
	private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emissores =
			new ConcurrentHashMap<>();

	@Transactional
	public PontoRastreamentoEntregaDTO publicar(
			Usuario funcionario,
			Long quiosqueId,
			UUID pedidoId,
			LocalizacaoEntregaCreateDTO data) {
		Quiosque quiosque = buscarQuiosqueComAcesso(funcionario, quiosqueId);
		Pedido pedido = pedidoRepository.findByIdAndQuiosque(pedidoId, quiosque)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

		validarEntregaAtiva(pedido);
		if (pedido.getIdAparelhoEntregador() == null
				|| !pedido.getIdAparelhoEntregador().equals(data.idAparelhoEntregador())) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN,
					"Este aparelho nao iniciou a entrega do pedido");
		}
		validarHorario(data.capturadoEm());
		validarPrecisao(data.precisaoMetros());

		var ultimo = pontoRepository.findFirstByPedidoOrderByCapturadoEmDesc(pedido);
		if (ultimo.isPresent()) {
			PontoRastreamentoEntrega anterior = ultimo.get();
			if (!data.capturadoEm().isAfter(anterior.getCapturadoEm())) {
				return PontoRastreamentoEntregaDTO.from(anterior);
			}

			long distancia = distanciaMetros(anterior, data);
			long segundos = Math.max(
					1,
					Duration.between(anterior.getCapturadoEm(), data.capturadoEm()).getSeconds());
			double distanciaMinima = distanciaMinimaPorPrecisao(anterior, data);
			if (distancia < distanciaMinima
					&& segundos < INTERVALO_ESTACIONADO_SEGUNDOS) {
				return PontoRastreamentoEntregaDTO.from(anterior);
			}
			if ((double) distancia / segundos > VELOCIDADE_MAXIMA_METROS_SEGUNDO) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Deslocamento de GPS invalido");
			}
		}

		PontoRastreamentoEntrega ponto = pontoRepository.save(
				new PontoRastreamentoEntrega(
						pedido,
						data.latitude(),
						data.longitude(),
						data.precisaoMetros(),
						data.direcaoGraus(),
						data.velocidadeMetrosSegundo(),
						data.capturadoEm()));
		PontoRastreamentoEntregaDTO resposta = PontoRastreamentoEntregaDTO.from(ponto);
		emitirAposCommit(pedidoId, resposta);
		return resposta;
	}

	@Transactional
	public RastreamentoEntregaDTO buscar(Usuario cliente, UUID pedidoId) {
		Pedido pedido = buscarPedidoDoCliente(cliente, pedidoId);
		List<PontoRastreamentoEntrega> pontos = new ArrayList<>(
				pontoRepository.findTop500ByPedidoOrderByCapturadoEmDesc(pedido));
		pontos.sort((a, b) -> a.getCapturadoEm().compareTo(b.getCapturadoEm()));
		return RastreamentoEntregaDTO.from(pedido, pontos);
	}

	@Transactional
	public SseEmitter acompanhar(Usuario cliente, UUID pedidoId) {
		Pedido pedido = buscarPedidoDoCliente(cliente, pedidoId);
		SseEmitter emissor = new SseEmitter(0L);
		CopyOnWriteArrayList<SseEmitter> lista = emissores.computeIfAbsent(
				pedidoId, ignored -> new CopyOnWriteArrayList<>());
		lista.add(emissor);

		Runnable remover = () -> removerEmissor(pedidoId, emissor);
		emissor.onCompletion(remover);
		emissor.onTimeout(remover);
		emissor.onError(erro -> remover.run());

		try {
			// Define a reconexao sugerida mesmo quando ainda nao existe uma posicao.
			emissor.send(SseEmitter.event().comment("conectado").reconnectTime(3000));
			pontoRepository.findFirstByPedidoOrderByCapturadoEmDesc(pedido)
					.ifPresent(ponto -> enviar(emissor, PontoRastreamentoEntregaDTO.from(ponto)));
		} catch (RuntimeException | IOException erro) {
			remover.run();
			emissor.completeWithError(erro);
		}
		return emissor;
	}

	private Pedido buscarPedidoDoCliente(Usuario cliente, UUID pedidoId) {
		Pedido pedido = pedidoRepository.findByClienteAndId(cliente, pedidoId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Pedido nao encontrado"));
		validarEntregaAtiva(pedido);
		return pedido;
	}

	private Quiosque buscarQuiosqueComAcesso(Usuario usuario, Long quiosqueId) {
		Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Quiosque nao encontrado"));
		if (!quiosque.usuarioPossuiAcesso(usuario)) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, "Usuario nao possui acesso ao quiosque");
		}
		return quiosque;
	}

	private void validarEntregaAtiva(Pedido pedido) {
		if (pedido.getStatus() != StatusPedido.EM_ENTREGA) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT, "Pedido nao esta em entrega");
		}
	}

	private void validarHorario(Instant capturadoEm) {
		Instant agora = Instant.now();
		if (capturadoEm.isBefore(agora.minus(ATRASO_MAXIMO))
				|| capturadoEm.isAfter(agora.plus(ADIANTAMENTO_MAXIMO))) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Horario da localizacao invalido");
		}
	}

	private void validarPrecisao(Double precisaoMetros) {
		// A API repete a barreira do aplicativo para proteger o histórico de
		// versões antigas ou clientes que tentem publicar coordenadas diretamente.
		if (precisaoMetros == null
				|| precisaoMetros <= 0
				|| precisaoMetros > PRECISAO_MAXIMA_METROS) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Precisao do GPS insuficiente");
		}
	}

	private double distanciaMinimaPorPrecisao(
			PontoRastreamentoEntrega anterior,
			LocalizacaoEntregaCreateDTO atual) {
		// Quanto maior a margem de erro, maior deve ser o deslocamento antes de
		// persistir um novo passo. O teto evita congelar uma entrega em movimento.
		double precisaoReferencia = Math.max(
				anterior.getPrecisaoMetros(), atual.precisaoMetros());
		return Math.max(
				DISTANCIA_MINIMA_METROS,
				Math.min(
						DISTANCIA_MAXIMA_POR_PRECISAO_METROS,
						precisaoReferencia * FATOR_DISTANCIA_POR_PRECISAO));
	}

	private long distanciaMetros(
			PontoRastreamentoEntrega anterior,
			LocalizacaoEntregaCreateDTO atual) {
		return new Coordenada(anterior.getLatitude(), anterior.getLongitude())
				.distanciaAte(new Coordenada(atual.latitude(), atual.longitude()));
	}

	private void emitirAposCommit(UUID pedidoId, PontoRastreamentoEntregaDTO ponto) {
		Runnable emitir = () -> emitir(pedidoId, ponto);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
					new TransactionSynchronization() {
						@Override
						public void afterCommit() {
							emitir.run();
						}
					});
			return;
		}
		emitir.run();
	}

	private void emitir(UUID pedidoId, PontoRastreamentoEntregaDTO ponto) {
		List<SseEmitter> lista = emissores.get(pedidoId);
		if (lista == null) return;
		for (SseEmitter emissor : lista) {
			try {
				enviar(emissor, ponto);
			} catch (RuntimeException erro) {
				removerEmissor(pedidoId, emissor);
				emissor.completeWithError(erro);
			}
		}
	}

	private void enviar(SseEmitter emissor, PontoRastreamentoEntregaDTO ponto) {
		try {
			emissor.send(SseEmitter.event()
					.id(ponto.id().toString())
					.name("localizacao")
					.reconnectTime(3000)
					.data(ponto, MediaType.APPLICATION_JSON));
		} catch (IOException erro) {
			throw new IllegalStateException("Falha ao enviar localizacao por SSE", erro);
		}
	}

	private void removerEmissor(UUID pedidoId, SseEmitter emissor) {
		CopyOnWriteArrayList<SseEmitter> lista = emissores.get(pedidoId);
		if (lista == null) return;
		lista.remove(emissor);
		if (lista.isEmpty()) emissores.remove(pedidoId, lista);
	}
}
