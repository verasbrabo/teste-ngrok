package app.service;


//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.AtualizarStatusDTO;
import app.DTO.AtualizarProximidadeDTO;
import app.DTO.AvaliarDTO;
import app.DTO.ItemDTO;
import app.DTO.ItemPedidoDTO;
import app.DTO.MotivoCancelamentoDTO;
import app.DTO.PedidoDTO;
import app.DTO.PedidoInternoDTO;
import app.DTO.PedidoGetDTO;
import app.DTO.PedidoLoteDTO;
import app.DTO.PedidoCreateResponseDTO;
import app.DTO.ValidarCodigoDTO;
import app.auth.AuthRepository;
import app.config.NotificationRequestDTO;
import app.config.NotificationService;
import app.entity.Acompanhamento;
import app.entity.Cliente;
import app.entity.Coordenada;
import app.entity.Cupom;
import app.entity.CupomCliente;
import app.entity.Item;
import app.entity.ItemPedido;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.FormaPagamento;
import app.enums.StatusPagamento;
import app.enums.StatusPedido;
import app.enums.UserRole;
import app.repository.AcompanhamentoRepository;
import app.repository.CupomRepository;
import app.repository.ItemRepository;
import app.repository.PedidoRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class PedidoService {

	@Autowired
	PedidoRepository pedidoRepository;
	
	@Autowired
	QuiosqueRepository quiosqueRepository;
	
	@Autowired
	ItemRepository itemRepository;
	
	@Autowired
	AcompanhamentoRepository acompanhamentoRepository;
	
	@Autowired
	CupomRepository cupomRepository;
	
	@Autowired
	CupomClienteRepository cupomClienteRepository;
	
	@Autowired
	AuthRepository userRepository;
	
	@Autowired
	private NotificationService notificationService;
	
	public PedidoCreateResponseDTO rejeitarPedido(
			Usuario funcionario,
			Long id_quiosque,
			UUID id) {
		
		Quiosque quiosque = findQuiosqueForUser(funcionario, id_quiosque);
		
		Pedido pedido = pedidoRepository.findByIdAndQuiosque(id, quiosque)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		
		pedido.setStatus(StatusPedido.REJEITADO);	
		
		Cliente cliente = (Cliente) pedido.getCliente();
		
//		Cliente cliente;
//		if(pedido.getCliente() instanceof Cliente) 
//			cliente = (Cliente) pedido.getCliente();
//		else
//			return PedidoCreateResponseDTO.from(this.pedidoRepository.save(pedido));
		
		boolean possuiPedidoAtivo =
                pedidoRepository.existsByClienteIdAndStatusNotIn(
                		cliente.getId(),
                		StatusPedido.STATUS_FINALIZADOS
                );
		 if(!possuiPedidoAtivo) {
			 cliente.setCodigoEntrega(null);;
		 }
		 userRepository.save(cliente);
		 
		 
		 NotificationRequestDTO dto = NotificationRequestDTO.from(pedido);
		 notificationService.sendNotificationToDevice(dto);
		 return PedidoCreateResponseDTO.from(this.pedidoRepository.save(pedido));
		
	}
	
	@Transactional
	public PedidoGetDTO quiosqueCancelarPedido(Usuario usuario, Long id_quiosque, UUID id, MotivoCancelamentoDTO data) {
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		
		Pedido pedido = pedidoRepository.findByIdAndQuiosque(id, quiosque)
				 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));		
		
		if(!pedido.getCliente().getRole().equals(UserRole.CLIENTE)) {
			pedido.cancelar();
		} 
		
		
		if(!data.motivo().isBlank()) {
			pedido.setMotivoCancel(data.motivo());
			pedido.cancelar();
		}					
		
		if(!pedido.getStatus().equals(StatusPedido.CANCELADO)) {	
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Pedido não pode ser cancelado"
				);
		}
		
		Cliente cliente = (Cliente) pedido.getCliente();
		
	    boolean possuiPedidoAtivo =
                pedidoRepository.existsByClienteIdAndStatusNotIn(
                		cliente.getId(),
                		StatusPedido.STATUS_FINALIZADOS
                );
		 if(!possuiPedidoAtivo) {
			 cliente.setCodigoEntrega(null);;
		 }
		 
		userRepository.save(usuario);
		pedido = this.pedidoRepository.save(pedido);
		
		NotificationRequestDTO dto = NotificationRequestDTO.cancelado(pedido);
		notificationService.sendNotificationToDevice(dto);
		return PedidoGetDTO.from(pedido);
	}
	
	private String gerarCodigo(int tamanho) { 
		String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	    SecureRandom random = new SecureRandom();
	    StringBuilder codigo = new StringBuilder(tamanho);
	    for (int i = 0; i < tamanho; i++) {
            int index = random.nextInt(CARACTERES.length());
            codigo.append(CARACTERES.charAt(index));
	    }

        return codigo.toString();
	}
	
	@Transactional
	private String adicionarCodigo(Cliente cliente, Pedido pedido) {	 
		 boolean possuiPedidoAtivo =
	                pedidoRepository.existsByClienteIdAndStatusNotIn(
	                        cliente.getId(),
	                        StatusPedido.STATUS_FINALIZADOS
	                );
		 if(possuiPedidoAtivo) {
			 pedido.setCodigoEntrega(cliente.getCodigoEntrega());
			 return cliente.getCodigoEntrega();
		 }
		 
		 String codigo = gerarCodigo(4);
		 cliente.setCodigoEntrega(codigo);
		 userRepository.save(cliente);
		 pedido.setCodigoEntrega(codigo);
		 return codigo;
	}	
	
	@Transactional
	public List<PedidoGetDTO> createPedidosEmLote(Cliente cliente, PedidoLoteDTO data) {
		Set<Long> quiosques = new HashSet<>();
		for (PedidoDTO pedido : data.pedidos()) {
			// Um pedido por quiosque impede que dois cupons disputem o mesmo
			// subtotal dentro da mesma finalizacao.
			if (!quiosques.add(pedido.quiosque())) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Existe mais de um pedido para o mesmo quiosque");
			}
		}

		List<PedidoGetDTO> pedidos = new ArrayList<>();
		for (PedidoDTO pedido : data.pedidos()) {
			// A excecao de qualquer pedido permanece sem tratamento para que o
			// Spring reverta pedidos e usos de cupom ja preparados neste lote.
			pedidos.add(createMeuPedido(cliente, pedido));
		}
		return pedidos;
	}

	@Transactional
	public PedidoGetDTO createMeuPedido(
			Cliente cliente, 
			PedidoDTO data) {
		
		
		
		Quiosque quiosque = quiosqueRepository.findById(data.quiosque())
		        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

		Coordenada entrega = new Coordenada(
		        data.latitudeEntrega(),
		        data.longitudeEntrega());

		if (!quiosque.atende(entrega)) {

		    throw new ResponseStatusException(
		            HttpStatus.BAD_REQUEST,
		            "Fora do alcance ou horario de atendimento do quiosque");
		}
		
		
		
		
		Pedido pedido = new Pedido(
				LocalDateTime.now(),
		        entrega,
		        cliente,
		        quiosque,
		        new ArrayList<>(),
		        StatusPedido.CRIADO,
                data.idAparelho()
		);
		adicionarItens(pedido, data.itens(), quiosque);
		
		
		
		pedido.setCodigoEntrega(data.codigoEntrega());
		pedido.calcularValorSubTotal();
		
		
		
		if(data.id_cupom() != null) {
			Cupom cupom = this.cupomRepository.findByIdAndQuiosqueId(data.id_cupom(), data.quiosque())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado"));
			Boolean jaUtilizou = cupomClienteRepository.existsByCupomIdAndClienteId(data.id_cupom(), cliente.getId());
			if(cupom.podeSerAplicado(pedido.getSubTotal()) && !jaUtilizou) {
				pedido.setCupom(cupom);
			
				CupomCliente cc = new CupomCliente(cupom, cliente);

				cc.setCliente(cliente);
				cc.setCupom(cupom);
				
				cupomClienteRepository.save(cc);
				
			} else {
				 throw new ResponseStatusException(
						 HttpStatus.CONFLICT, 
						 "Cupom não pode ser aplicado");
			}
			
		}
			
		pedido.calcularValorTotal();
		
		// O pagamento depende do valor total real do pedido, entao so pode ser
		// validado depois que os itens e adicionais foram processados.
		configurarPagamento(pedido, data);
		Long tempEstimado = quiosque.calcularTempoEstimado();
		pedido.setTempoEstimado(tempEstimado);
		if(pedido.getCodigoEntrega() == null)
			adicionarCodigo(cliente, pedido);

		Pedido salvo = pedidoRepository.save(pedido);
		// Avisa o quiosque (push/FCM) para a lista de pedidos atualizar sozinha.
		notificarQuiosqueAposCommit(quiosque, salvo, "Novo pedido recebido",
				"Pedido de " + cliente.getNome());
		return PedidoGetDTO.from(salvo);
	}
	
	private void configurarPagamento(Pedido pedido, PedidoDTO data) {
		FormaPagamento formaPagamento = data.formaPagamento();
		// O cliente nao pode criar pedido sem orientar como pretende pagar na
		// entrega.
		if (formaPagamento == null) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Forma de pagamento obrigatoria");
		}

		// Todo pagamento nasce pendente porque o recebimento acontece na entrega.
		pedido.setFormaPagamento(formaPagamento);
		pedido.setStatusPagamento(StatusPagamento.PENDENTE);
		pedido.setPrecisaMaquininha(formaPagamento.precisaMaquininha());

		// Pix/cartao nao precisam de valor em dinheiro nem troco. Cartao ja fica
		// marcado por precisaMaquininha.
		if (formaPagamento != FormaPagamento.DINHEIRO) {
			pedido.setValorPagoCliente(null);
			pedido.setValorTroco(null);
			return;
		}

		BigDecimal valorPagoCliente = data.valorPagoCliente();
		// Para dinheiro, o app precisa informar quanto o cliente vai entregar
		// para que o quiosque prepare o troco.
		if (valorPagoCliente == null) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Informe o valor em dinheiro");
		}

		valorPagoCliente = valorPagoCliente.setScale(2, RoundingMode.HALF_UP);
		// Validacao oficial: mesmo que o app falhe ou seja burlado, a API nao
		// aceita dinheiro menor que o total real do pedido.
		if (valorPagoCliente.compareTo(pedido.getValorTotal()) < 0) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Valor em dinheiro menor que o total do pedido");
		}

		pedido.setValorPagoCliente(valorPagoCliente);
		// Troco oficial persistido. O app cliente nunca envia esse valor porque
		// o total real pertence ao servidor.
		pedido.setValorTroco(
				valorPagoCliente.subtract(pedido.getValorTotal())
						.setScale(2, RoundingMode.HALF_UP));
	}


	/**
	 * Envia uma notificação push para todos os aparelhos do quiosque
	 * (proprietário + funcionários com deviceToken registrado). Usada para o
	 * app do quiosque atualizar a lista de pedidos automaticamente.
	 */
	private void notificarQuiosque(Quiosque quiosque, Pedido pedido, String titulo, String corpo) {
		Set<String> tokens = new HashSet<>();
		if (    quiosque.getProprietario() != null
				&& quiosque.getProprietario().getDeviceToken() != null)
			tokens.add(quiosque.getProprietario().getDeviceToken());
		if (quiosque.getFuncionarios() != null)
			for (Usuario f : quiosque.getFuncionarios())
				if (f.getDeviceToken() != null)
					tokens.add(f.getDeviceToken());
		for (String token : tokens)
			notificationService.sendNotificationToDevice(
					NotificationRequestDTO.novoPedido(token, titulo, corpo, pedido.getId().toString()));
	}

	/**
	 * Registra a notificacao do novo pedido somente depois do commit. Assim um
	 * lote revertido nao aparece no app do quiosque como pedido inexistente.
	 */
	private void notificarQuiosqueAposCommit(
			Quiosque quiosque,
			Pedido pedido,
			String titulo,
			String corpo) {
		Set<String> tokens = new HashSet<>();
		if (quiosque.getProprietario() != null
				&& quiosque.getProprietario().getDeviceToken() != null) {
			tokens.add(quiosque.getProprietario().getDeviceToken());
		}
		if (quiosque.getFuncionarios() != null) {
			for (Usuario funcionario : quiosque.getFuncionarios()) {
				if (funcionario.getDeviceToken() != null) {
					tokens.add(funcionario.getDeviceToken());
				}
			}
		}

		List<NotificationRequestDTO> notificacoes = tokens.stream()
				.map(token -> NotificationRequestDTO.novoPedido(
						token, titulo, corpo, pedido.getId().toString()))
				.toList();
		Runnable enviar = () -> notificacoes.forEach(notificationService::sendNotificationToDevice);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					enviar.run();
				}
			});
			return;
		}
		enviar.run();
	}

	/**
	 * Cria um pedido interno (balcão), feito pelo próprio quiosque.
	 *
	 * Ao contrário de {@link #createMeuPedido}, o quiosque é resolvido a partir do
	 * funcionário autenticado (sem precisar do id no corpo) e não há coordenada de
	 * entrega nem código de verificação. O pedido já entra como PREPARANDO, pois
	 * foi o próprio quiosque que o registrou (não precisa ser aceito).
	 */
	@Transactional
	public PedidoGetDTO createPedidoInterno(Usuario funcionario, Long id_quiosque, PedidoInternoDTO data) {

		Quiosque quiosque = findQuiosqueForUser(funcionario, id_quiosque);

		Pedido pedido = new Pedido(
				LocalDateTime.now(),
				null,            // pedido de balcão: sem coordenada de entrega
				funcionario,     // o funcionário figura como solicitante do pedido
				quiosque,
				new ArrayList<>(),
				StatusPedido.PREPARANDO,
                null
		);

		pedido.setInterno(true);
		// Pedido de balcão já entra em PREPARANDO: registra o marco.
		pedido.setDataHoraPreparando(LocalDateTime.now());
		String nome = (data.nomeCliente() != null && !data.nomeCliente().isBlank())
				? data.nomeCliente().trim()
				: "Balcão";
		pedido.setNomeCliente(nome);

		adicionarItens(pedido, data.itens(), quiosque);

		pedido.calcularValorSubTotal();
		pedido.calcularValorTotal();
		pedido.setTempoEstimado(quiosque.calcularTempoEstimado());

		return PedidoGetDTO.from(pedidoRepository.save(pedido));
	}

	/**
	 * Valida os itens informados e os adiciona ao [pedido], calculando os
	 * subtotais. Compartilhado entre o pedido do cliente e o pedido interno.
	 */
	private void adicionarItens(Pedido pedido, List<ItemPedidoDTO> itensDTO, Quiosque quiosque) {

		List<Long> itemIds = itensDTO.stream().map(ItemPedidoDTO::itemId).toList();

		List<Item> itensBanco = itemRepository.findAllById(itemIds);
		if (itensBanco.size() != new HashSet<>(itemIds).size())
		    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Um ou mais itens não foram encontrados");

		Map<Long, Item> itensMap = itensBanco.stream()
		        .collect(Collectors.toMap(Item::getId, Function.identity()));

		for (ItemPedidoDTO itemDTO : itensDTO) {

		    Item item = itensMap.get(itemDTO.itemId());

		    BigDecimal valor = item.getValorFinal();

		    if (!item.pertenceAoQuiosque(quiosque))
		        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item não pertence ao quiosque");

		    ItemPedido itemPedido = new ItemPedido(itemDTO.quantidade(),valor,item);

		    if(itemDTO.acompanhamentosid() != null && !itemDTO.acompanhamentosid().isEmpty()) {
		    	// problema de N+1, devo usar só um acesso ao banco pros acompanhamentos
		    	List<Acompanhamento> acompanhamentos = this.acompanhamentoRepository.findByIdInAndItemsContainsAndQuiosque(itemDTO.acompanhamentosid(), item, quiosque);
		    	if (acompanhamentos.size() != itemDTO.acompanhamentosid().size()) {
		    	    throw new ResponseStatusException(
		    	            HttpStatus.NOT_FOUND,
		    	            "Um ou mais acompanhamentos não foram encontrados");
		    	}
		    	itemPedido.adicionarAcompanhamentos(acompanhamentos);

		    }

//		    if(itemDTO.ingredientesid() != null && !itemDTO.ingredientesid().isEmpty()) {
//		    	List<Ingrediente> ingredientes= this.ingredienteRepository.findAllById(itemDTO.ingredientesid());
//
//		    	itemPedido.removerIngredientes(ingredientes);
//		    }
		    itemPedido.setIngredientesRemovido(String.join(";", itemDTO.ingredientes()) );

		    itemPedido.calcularSubtotal();

		    pedido.addItem(itemPedido);
		}
	}

	@Transactional
	public String cancelarPedido(Usuario usuario, UUID id) {
		Pedido pedido = pedidoRepository.findByClienteAndId(usuario, id)
				 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		
		pedido.cancelar();
		if(!pedido.getStatus().equals(StatusPedido.CANCELADO)) {	
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Pedido não pode ser cancelado"
				);
		}
		
		    
	    boolean possuiPedidoAtivo =
                pedidoRepository.existsByClienteIdAndStatusNotIn(
                		usuario.getId(),
                		StatusPedido.STATUS_FINALIZADOS
                );
	     Cliente cliente = (Cliente) usuario;
		 if(!possuiPedidoAtivo) {
			 
			 cliente.setCodigoEntrega(null);;
		 }
		userRepository.save(usuario);
		pedido = this.pedidoRepository.save(pedido);
		// Avisa o quiosque para a lista de pedidos atualizar sozinha.
		notificarQuiosque(pedido.getQuiosque(), pedido, "Pedido cancelado",
				"O cliente cancelou um pedido");
		return pedido.getStatus().toString();
	}
	
	public Pedido getMeuPedido(
			Usuario cliente,
			UUID id) {
		
		return this.pedidoRepository.findByClienteAndId(cliente, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		
	}
	
	public List<Pedido> getMeusPedidos(
			Usuario cliente, 
			StatusPedido status
//			,Pageable pageable
			) {
		
		
		return status == null
			? this.pedidoRepository.findByClienteOrderByDataHoraPedido(cliente)
		    : this.pedidoRepository.findByClienteAndStatusOrderByDataHoraPedido(cliente, status);
	}
	
	public List<Pedido> getMeusPedidosAtivos(
			Usuario cliente 
//			,Pageable pageable
			) {
		
		
		return this.pedidoRepository.findByClienteAndStatusNotInOrderByDataHoraPedido(cliente, StatusPedido.STATUS_FINALIZADOS);
	}	
	
	public List<Pedido> getPedidos(
			Usuario administrador,
			Long id_quiosque,
			StatusPedido status
//			,Pageable pageable
			) {
		
		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);		
		
		return status == null
				? this.pedidoRepository.findByQuiosqueOrderByDataHoraPedido(quiosque)
			    : this.pedidoRepository.findByQuiosqueAndStatusOrderByDataHoraPedido(quiosque, status);
	}
	
	public List<Pedido> getPedidosAtivos(
			Usuario administrador,
			Long id_quiosque
//			,Pageable pageable
			) {
		
		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);				
		return this.pedidoRepository.findByQuiosqueAndStatusInOrderByDataHoraPedido(quiosque, List.of(StatusPedido.CRIADO, StatusPedido.ACEITO, StatusPedido.PREPARANDO, StatusPedido.EM_ENTREGA));

	}
	
	public Pedido getPedidoEntregador(
			Usuario entregador,
			Long id_quiosque) {
		Quiosque quiosque = findQuiosqueForUser(entregador, id_quiosque);	
		
		return this.pedidoRepository.findByQuiosqueIdAndEntregadorIdAndStatusNotIn(quiosque.getId(), entregador.getId(), StatusPedido.STATUS_FINALIZADOS)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		
	}
	
	
	@Transactional
	public PedidoGetDTO updatePedido(
			UUID id, 
			Long id_quiosque,
			Usuario administrador, 
			AtualizarStatusDTO data) {	
		
		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);
		
		Pedido pedido = pedidoRepository.findByIdAndQuiosque(id, quiosque)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));

		// Pedido de balcão não passa por entrega/validação de código, então o
		// próprio quiosque pode finalizá-lo direto (a máquina de estados normal
		// só leva a FINALIZADO via validarCodigo).
		boolean finalizarInterno = pedido.isInterno()
				&& data.status() == StatusPedido.FINALIZADO
				&& !StatusPedido.STATUS_FINALIZADOS.contains(pedido.getStatus());

		if(pedido.getStatus().podeIrPara(data.status()) || finalizarInterno) {
			if(finalizarInterno)
				pedido.setDataHoraEntrega(LocalDateTime.now());
			// Marca quando o pedido entrou em PREPARANDO: base da janela de
			// cancelamento de 30 min para o cliente.
			if(data.status() == StatusPedido.PREPARANDO
					&& pedido.getDataHoraPreparando() == null)
				pedido.setDataHoraPreparando(LocalDateTime.now());
			// Quando o pedido sai para entrega, persiste qual aparelho do
			// entregador esta conduzindo a entrega.
			if(data.status() == StatusPedido.EM_ENTREGA
					&& data.idAparelhoEntregador() != null)
				pedido.setIdAparelhoEntregador(data.idAparelhoEntregador());
			pedido.setStatus(data.status());
			NotificationRequestDTO dto;
			if(data.status().equals(StatusPedido.EM_ENTREGA) && pedido.getEntregador() != null) 
				 dto = NotificationRequestDTO.entregador(pedido);
			else 
				 dto = NotificationRequestDTO.from(pedido);
			
			notificationService.sendNotificationToDevice(dto);
			return PedidoGetDTO.from(this.pedidoRepository.save(pedido));
		}
		
		throw new ResponseStatusException(
			    HttpStatus.BAD_REQUEST,
			    "Erro ao atualizar status"
			);			
	}

	@Transactional
	public PedidoGetDTO atualizarProximidadeEntrega(
			UUID id,
			Long id_quiosque,
			Usuario funcionario,
			AtualizarProximidadeDTO data) {

		Quiosque quiosque = findQuiosqueForUser(funcionario, id_quiosque);
		Pedido pedido = pedidoRepository.findByIdAndQuiosque(id, quiosque)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nÃ£o encontrado"));

		if (pedido.getStatus() != StatusPedido.EM_ENTREGA)
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido nÃ£o estÃ¡ em entrega");

		// RSSI publicado pelo app do quiosque. E dado temporario de proximidade;
		// a confirmacao real da entrega continua sendo feita por codigo.
		pedido.setRssiMedioEntrega(data.rssiMedio());
		return PedidoGetDTO.from(pedidoRepository.save(pedido));
	}
	
	@Transactional
	public PedidoGetDTO entregadorPedido(Usuario entregador, Long id_quiosque, UUID id) {
		Quiosque quiosque = findQuiosqueForUser(entregador, id_quiosque);	
		
		boolean possuiEntregasAtivas = this.pedidoRepository.existsByEntregadorIdAndStatusNotIn(entregador.getId(), StatusPedido.STATUS_FINALIZADOS);
		
		if(possuiEntregasAtivas)
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Já possui entregas em andamento"
				);
		
		
			
		Pedido pedido = this.pedidoRepository.findByIdAndQuiosque(id, quiosque)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		
		if(StatusPedido.STATUS_FINALIZADOS.contains(pedido.getStatus()))
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Pedido indisponivel"
				);
		
		if(pedido.getEntregador() != null)
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Pedido já possui entregador"
				);
		
		pedido.setEntregador(entregador);
		
		return PedidoGetDTO.from(this.pedidoRepository.save(pedido));
	}
	
	public String getCodigo(
			Usuario cliente,
			UUID id) {
		
		
		Pedido pedido = pedidoRepository.findByClienteAndId(cliente, id)
				 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		StatusPedido status = pedido.getStatus();
		if (StatusPedido.CANCELADO.equals(status) || StatusPedido.FINALIZADO.equals(status) || StatusPedido.AVALIADO.equals(status))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido não está disponível para obter código");
		return pedido.getCodigoEntrega();
	}
	
	@Transactional
	public boolean validarCodigo(Usuario funcionario, Long id_quiosque, UUID id, ValidarCodigoDTO data) {
	    // Quem finaliza é o próprio quiosque (não um entregador designado): busca
	    // o pedido pelo quiosque do funcionário autenticado.
	    Quiosque quiosque = findQuiosqueForUser(funcionario, id_quiosque);
	    Pedido pedido = pedidoRepository
	        .findByIdAndQuiosque(id, quiosque)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Pedido não encontrado"));

	    // A validação de código só vale para pedidos de cliente que já saíram
	    // para entrega (EM_ENTREGA). Pedidos de balcão (interno) são finalizados
	    // direto, sem código.
	    if (pedido.isInterno() || pedido.getStatus() != StatusPedido.EM_ENTREGA)
	        throw new ResponseStatusException(
	            HttpStatus.CONFLICT, "Pedido não está em entrega");

	    boolean isValid = pedido.getCodigoEntrega().equals(data.codigo());
	    
	    if(!isValid)
	    	return false;
	    
	    pedido.setDataHoraEntrega(LocalDateTime.now());
	    pedido.setStatus(StatusPedido.FINALIZADO);
	    
	    Quiosque q = pedido.getQuiosque();
	    
	    Long tempoEntrega = Duration.between(pedido.getDataHoraPedido(), pedido.getDataHoraEntrega()).toMinutes();
	    
	    q.setQtdPedidosFinalizados(q.getQtdPedidosFinalizados()+1);
	    q.setSomaTempoEntrega(q.getSomaTempoEntrega()+tempoEntrega);
	    
	    pedidoRepository.saveAndFlush(pedido);

	    notificationService.sendNotificationToDevice(NotificationRequestDTO.from(pedido));

	    Cliente cliente = (Cliente) pedido.getCliente();
	    
	    boolean possuiPedidoAtivo =
                pedidoRepository.existsByClienteIdAndStatusNotIn(
                		cliente.getId(),
                		StatusPedido.STATUS_FINALIZADOS
                );
		 if(!possuiPedidoAtivo) {
			 cliente.setCodigoEntrega(null);;
		 }
		userRepository.save(cliente);
	    
	    return true;
	}
	
	@Transactional
	public String avaliarPedido(Usuario usuario, UUID id, AvaliarDTO data) {
		Pedido pedido = pedidoRepository.findByClienteAndId(usuario, id)
				 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Pedido não encontrado")));
		
		if(!usuario.getRole().equals(UserRole.CLIENTE)) 
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apenas usuarios não relacionados ao quiosque podem avaliar o pedido");
		
		if (data.comentario() != null && !data.comentario().isBlank()) {
		    pedido.avaliar(data.nota(), data.comentario());
		} else {
		    pedido.avaliar(data.nota());
		}
		
		if(pedido.getNota() == null) {	
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Valor invalido"
				);
		}
		if(!data.comentario().equals(pedido.getComentario())) {	
			throw new ResponseStatusException(
				    HttpStatus.BAD_REQUEST,
				    "Comentario invalido"
				);
		}
		
		Quiosque q = pedido.getQuiosque();
		
		q.addAvaliacao(data.nota());
		
		this.quiosqueRepository.save(q);
		
		pedido = this.pedidoRepository.save(pedido);
		return pedido.getStatus().getDescricao();		
	}
	
	private Quiosque findQuiosqueForUser(Usuario usuario, Long id) {

	    Quiosque quiosque = quiosqueRepository.findById(id)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

	    if (!quiosque.usuarioPossuiAcesso(usuario)) {
	        throw new ResponseStatusException(
	            HttpStatus.FORBIDDEN, "Usuário não tem acesso a esse quiosque");
	    }

	    return quiosque;
	}

}
