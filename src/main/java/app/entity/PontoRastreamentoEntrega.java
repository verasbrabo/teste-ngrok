package app.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Ponto amostrado do trajeto real do entregador.
 */
@Entity
@Table(
		name = "ponto_rastreamento_entrega",
		indexes = @Index(
				name = "idx_rastreamento_pedido_data",
				columnList = "pedido_id,capturado_em"))
public class PontoRastreamentoEntrega {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_id", nullable = false)
	private Pedido pedido;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Column(name = "precisao_metros", nullable = false)
	private Double precisaoMetros;

	@Column(name = "direcao_graus")
	private Double direcaoGraus;

	@Column(name = "velocidade_metros_segundo")
	private Double velocidadeMetrosSegundo;

	@Column(name = "capturado_em", nullable = false)
	private Instant capturadoEm;

	protected PontoRastreamentoEntrega() {
	}

	public PontoRastreamentoEntrega(
			Pedido pedido,
			Double latitude,
			Double longitude,
			Double precisaoMetros,
			Double direcaoGraus,
			Double velocidadeMetrosSegundo,
			Instant capturadoEm) {
		this.pedido = pedido;
		this.latitude = latitude;
		this.longitude = longitude;
		this.precisaoMetros = precisaoMetros;
		this.direcaoGraus = direcaoGraus;
		this.velocidadeMetrosSegundo = velocidadeMetrosSegundo;
		this.capturadoEm = capturadoEm;
	}

	public Long getId() {
		return id;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getPrecisaoMetros() {
		return precisaoMetros;
	}

	public Double getDirecaoGraus() {
		return direcaoGraus;
	}

	public Double getVelocidadeMetrosSegundo() {
		return velocidadeMetrosSegundo;
	}

	public Instant getCapturadoEm() {
		return capturadoEm;
	}
}
