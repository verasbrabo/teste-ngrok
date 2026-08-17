package app.entity;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.enums.FormaPagamento;
import app.enums.StatusPagamento;
import app.enums.StatusPedido;
import app.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.core.io.BigDecimalParser;

@SuppressWarnings("unused")
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Pedido {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
    //Id de conexão de para feture de aproximação
	private BigDecimal valorTotal;
	private LocalDateTime dataHoraPedido;
	private LocalDateTime dataHoraEntrega;
	// Momento em que o pedido entrou em PREPARANDO (quiosque aceitou). Base da
	// janela de 30 min para o cliente poder cancelar. Nulo enquanto CRIADO.
	private LocalDateTime dataHoraPreparando;
	private Long tempoEstimado; // minutos
	@Embedded
	private Coordenada coordenada;
	private String codigoEntrega;
	private Integer nota;
	private String motivoCancel;
	// Pedido feito pelo próprio quiosque (balcão): sem cliente externo, sem
	// coordenada de entrega e sem código de verificação. Nullable para conviver
	// com pedidos antigos (ddl-auto=update); ver isInterno().
	private Boolean interno;
	// Nome avulso exibido para pedidos de balcão (ex.: "Balcão", "Mesa 3").
	private String nomeCliente;

	@ManyToOne
	@JoinColumn(name = "idCliente")
	private Usuario cliente;
	
	@ManyToOne
	@JoinColumn(name = "idQuiosque")
	private Quiosque quiosque;
	
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
	private List<ItemPedido> itemPedidos;
	
	@ManyToOne
	@JoinColumn(name = "idEntregador")
	private Usuario entregador;
	
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private StatusPedido status;		

    // UUID persistente do aparelho do cliente. O app do cliente anuncia esse
    // valor por BLE, e o app do quiosque usa o mesmo UUID para filtrar o scan.
    @Column (name = "IdDevice", nullable = true)
    private UUID aparelhoId;

    // UUID persistente do aparelho do entregador que saiu com o pedido.
    // Serve para auditoria/identificacao do aparelho, nao para o scan principal.
    @Column(name = "IdDeviceEntregador", nullable = true)
    private UUID aparelhoEntregadorId;

    // RSSI medio calculado pelo quiosque durante a aproximacao. Campo opcional:
    // a tela do entregador funciona localmente mesmo sem persistir esse valor.
    @Column(name = "RssiMedioEntrega", nullable = true)
    private Integer rssiMedioEntrega;

    // Forma escolhida pelo cliente antes de criar o pedido. Representa a
    // intencao de pagamento na entrega, nao pagamento online confirmado.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20, nullable = true)
    private FormaPagamento formaPagamento;

    // Estado do recebimento. Para o fluxo atual, novos pedidos entram PENDENTE
    // e o funcionario confirma o pagamento na entrega em evolucao futura.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20, nullable = true)
    private StatusPagamento statusPagamento;

    // Valor que o cliente informou que vai entregar em dinheiro. Nulo para Pix
    // e cartao, porque nao existe calculo de troco nesses casos.
    @Column(precision = 10, scale = 2, nullable = true)
    private BigDecimal valorPagoCliente;

    // Troco oficial calculado pela API usando valorPagoCliente - valorTotal.
    // Nunca confiamos em troco vindo do app cliente.
    @Column(precision = 10, scale = 2, nullable = true)
    private BigDecimal valorTroco;

    // Flag derivada da forma de pagamento para facilitar exibicao no quiosque.
    @Column(nullable = true)
    private Boolean precisaMaquininha;

	public void calcularValorTot() {
		if (itemPedidos == null || itemPedidos.isEmpty()) {
	        this.valorTotal = BigDecimal.ZERO;
	        return;
	    }
		this.setValorTotal(itemPedidos.stream()
		        .map(ItemPedido::getSubTotal)
		        .reduce(BigDecimal.ZERO, BigDecimal::add));
	}

	public Pedido(LocalDateTime dataHoraPedido, Coordenada coordenada,
			Usuario cliente, Quiosque quiosque, List<ItemPedido> itemPedidos,StatusPedido status , UUID aparelhoId) {
		super();
        this.aparelhoId = aparelhoId;
		this.dataHoraPedido = dataHoraPedido;
		this.coordenada = coordenada;
		this.cliente = cliente;
		this.quiosque = quiosque;
		this.itemPedidos = itemPedidos;
		this.status = status;
	}
	
	public void addItem(ItemPedido item) {
	    item.setPedido(this);
	    this.itemPedidos.add(item);
	}
	
	 public void cancelar() {
	        // A janela de 30 min conta a partir de quando o pedido entrou em
	        // PREPARANDO (não da criação do pedido).
	        boolean passou30Min =
	        		dataHoraPreparando != null
	        		&& dataHoraPreparando.isBefore(LocalDateTime.now().minusMinutes(30));

	        boolean possuiMotivo = motivoCancel != null;

	        boolean clienteEFuncionario = !cliente.getRole().equals(UserRole.CLIENTE);

	        // Regras de cancelamento pelo cliente:
	        // - CRIADO ou ACEITO (aguardando o quiosque iniciar o preparo): pode
	        //   cancelar a qualquer momento.
	        // - PREPARANDO ou EM_ENTREGA: só pode cancelar após 30 min do pedido.
	        // - demais status (finalizado/rejeitado/avaliado/já cancelado): não cancela.
	        boolean clientePodeCancelar =
	            status == StatusPedido.CRIADO
	            || status == StatusPedido.ACEITO
	            || ((status == StatusPedido.PREPARANDO
	                    || status == StatusPedido.EM_ENTREGA)
	                && passou30Min);

	        if (clientePodeCancelar || possuiMotivo || clienteEFuncionario) {
	        	this.status = StatusPedido.CANCELADO;
	        }
	 }
	 
	 public void avaliar(int nota) {
		 
		 if(nota >= 1 && nota <= 5 && this.getStatus().equals(StatusPedido.FINALIZADO)) {
			 this.nota = nota;
			 this.status = StatusPedido.AVALIADO;
		 }
	 }
	 


	 public UUID getId() {
		 return id;
	 }

     public UUID getIdAparelho(){return aparelhoId;}

     public UUID getIdAparelhoEntregador(){return aparelhoEntregadorId;}

    public void setIdAparelho(){this.aparelhoId = aparelhoId;}

    public void setIdAparelhoEntregador(UUID aparelhoEntregadorId) {
        this.aparelhoEntregadorId = aparelhoEntregadorId;
    }

    public Integer getRssiMedioEntrega() {
        return rssiMedioEntrega;
    }

    public void setRssiMedioEntrega(Integer rssiMedioEntrega) {
        this.rssiMedioEntrega = rssiMedioEntrega;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public BigDecimal getValorPagoCliente() {
        return valorPagoCliente;
    }

    public void setValorPagoCliente(BigDecimal valorPagoCliente) {
        this.valorPagoCliente = valorPagoCliente;
    }

    public BigDecimal getValorTroco() {
        return valorTroco;
    }

    public void setValorTroco(BigDecimal valorTroco) {
        this.valorTroco = valorTroco;
    }

    public boolean isPrecisaMaquininha() {
        return precisaMaquininha != null && precisaMaquininha;
    }

    public void setPrecisaMaquininha(boolean precisaMaquininha) {
        this.precisaMaquininha = precisaMaquininha;
    }


    public void setId(UUID id) {
		 this.id = id;
	 }

	 public BigDecimal getValorTotal() {
		 return valorTotal;
	 }

	 public void setValorTotal(BigDecimal valorTotal) {
		 this.valorTotal = valorTotal;
	 }

	 public LocalDateTime getDataHoraPedido() {
		 return dataHoraPedido;
	 }

	 public void setDataHoraPedido(LocalDateTime dataHoraPedido) {
		 this.dataHoraPedido = dataHoraPedido;
	 }

	 public LocalDateTime getDataHoraEntrega() {
		 return dataHoraEntrega;
	 }

	 public void setDataHoraEntrega(LocalDateTime dataHoraEntrega) {
		 this.dataHoraEntrega = dataHoraEntrega;
	 }

	 public LocalDateTime getDataHoraPreparando() {
		 return dataHoraPreparando;
	 }

	 public void setDataHoraPreparando(LocalDateTime dataHoraPreparando) {
		 this.dataHoraPreparando = dataHoraPreparando;
	 }

	 public Long getTempoEstimado() {
		 return tempoEstimado;
	 }

	 public void setTempoEstimado(Long tempoEstimado) {
		 this.tempoEstimado = tempoEstimado;
	 }

	 public Coordenada getCoordenada() {
		 return coordenada;
	 }

	 public void setCoordenada(Coordenada coordenada) {
		 this.coordenada = coordenada;
	 }

	 public String getCodigoEntrega() {
		 return codigoEntrega;
	 }

	 public void setCodigoEntrega(String codigoEntrega) {
		 this.codigoEntrega = codigoEntrega;
	 }

	 public Integer getNota() {
		 return nota;
	 }

	 public void setNota(Integer nota) {
		 this.nota = nota;
	 }

	 public String getMotivoCancel() {
		 return motivoCancel;
	 }

	 public void setMotivoCancel(String motivoCancel) {
		 this.motivoCancel = motivoCancel;
	 }

	 /** Verdadeiro para pedidos de balcão feitos pelo próprio quiosque. */
	 public boolean isInterno() {
		 return interno != null && interno;
	 }

	 public void setInterno(boolean interno) {
		 this.interno = interno;
	 }

	 public String getNomeCliente() {
		 return nomeCliente;
	 }

	 public void setNomeCliente(String nomeCliente) {
		 this.nomeCliente = nomeCliente;
	 }

	 public Usuario getCliente() {
		 return cliente;
	 }

	 public void setCliente(Usuario cliente) {
		 this.cliente = cliente;
	 }

	 public Quiosque getQuiosque() {
		 return quiosque;
	 }

	 public void setQuiosque(Quiosque quiosque) {
		 this.quiosque = quiosque;
	 }

	 public List<ItemPedido> getItemPedidos() {
		 return itemPedidos;
	 }

	 public void setItemPedidos(List<ItemPedido> itemPedidos) {
		 this.itemPedidos = itemPedidos;
	 }

	 public Usuario getEntregador() {
		 return entregador;
	 }

	 public void setEntregador(Usuario entregador) {
		 this.entregador = entregador;
	 }

	 public StatusPedido getStatus() {
		 return status;
	 }

	 public void setStatus(StatusPedido status) {
		 this.status = status;
	 }

	 public Pedido(UUID id, BigDecimal valorTotal, LocalDateTime dataHoraPedido, LocalDateTime dataHoraEntrega,
			Long tempoEstimado, Coordenada coordenada, String codigoEntrega, Integer nota, String motivoCancel,
			Usuario cliente, Quiosque quiosque, List<ItemPedido> itemPedidos, Usuario entregador, StatusPedido status) {
		super();
		this.id = id;
		this.valorTotal = valorTotal;
		this.dataHoraPedido = dataHoraPedido;
		this.dataHoraEntrega = dataHoraEntrega;
		this.tempoEstimado = tempoEstimado;
		this.coordenada = coordenada;
		this.codigoEntrega = codigoEntrega;
		this.nota = nota;
		this.motivoCancel = motivoCancel;
		this.cliente = cliente;
		this.quiosque = quiosque;
		this.itemPedidos = itemPedidos;
		this.entregador = entregador;
		this.status = status;
	 }

	 public Pedido() {
		super();
	 }
	
}
