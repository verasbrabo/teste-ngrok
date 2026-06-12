package app.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import app.DTO.QuiosqueDTO;
import app.DTO.QuiosqueUpdateDTO;
import app.enums.StatusConta;
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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@SuppressWarnings("unused")
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Quiosque {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	private String nome;
	@Column(unique = true, nullable = false)
	private String email;
	@Column(unique = true, nullable = false)
	private String cnpj;
	@NotNull
	private LocalTime openingTime;
	@NotNull
	private LocalTime closingTime;
	@NotNull
	private Long distAtendimento;
	
	private Long somaTempoEntrega;
	private Long qtdPedidosFinalizados; 
	
	@Transient
	private Long distancia;
	
	@Embedded
	private Endereco endereco;
	@Embedded
	private AvaliacaoResumo avaliacaoResumo;
	@Embedded
	private Coordenada coordenada;

	@OneToOne
	@JoinColumn(name = "imagem_id")
	private Imagem imagem;
	
	@OneToOne
	@JoinColumn(name = "proprietario_id")
	private Usuario proprietario;
	
	@OneToMany(mappedBy = "quiosque", cascade = CascadeType.ALL)
	private List<Usuario> funcionarios;
	
	@OneToMany(mappedBy = "quiosque", cascade = CascadeType.ALL)
    private List<Acompanhamento> acompanhamentos;
	
	public Quiosque(String nome, String email, String cnpj, LocalTime openingTime, LocalTime closingTime, Long distAtendimento, Endereco endereco, AvaliacaoResumo avaliacaoResumo,
			Coordenada coordenada, Usuario proprietario, StatusConta status, Long somaTempoEntrega, Long qtdPedidosFinalizados) {
		super();
		this.nome = nome;
		this.email = email;
		this.cnpj = cnpj;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
		this.distAtendimento = distAtendimento;
		this.endereco = endereco;
		this.avaliacaoResumo = avaliacaoResumo;
		this.coordenada = coordenada;
		this.proprietario = proprietario;
		this.status = status;
		this.somaTempoEntrega = somaTempoEntrega;
		this.qtdPedidosFinalizados = qtdPedidosFinalizados;
	}


	@OneToMany(mappedBy = "quiosque")
	private List<Pedido> pedidos;

	@OneToMany(mappedBy = "quiosque", cascade = CascadeType.ALL)
	private List<Categoria> categorias;

	@Enumerated(EnumType.STRING)
	private StatusConta status;
	
	public boolean usuarioPossuiAcesso(Usuario usuario) {

	    return pertenceAoProprietario(usuario)
	            || possuiFuncionario(usuario);
	}
	
	public boolean pertenceAoProprietario(Usuario usuario) {

	    return usuario.getRole() == UserRole.PROPRIETARIO
	            && this.proprietario != null
	            && this.proprietario.getId().equals(usuario.getId());
	}
	
	public boolean possuiFuncionario(Usuario usuario) {

	    return this.funcionarios != null
	            && this.funcionarios.stream()
	                .anyMatch(func -> func.getId().equals(usuario.getId()));
	}
	
	public boolean atende(Coordenada entrega) {

		boolean atendeDist = this.coordenada.distanciaAte(entrega) <= this.distAtendimento;
		boolean atendeHora = atendeHorario();
		
	    return atendeDist && atendeHora;
	}
	
	private boolean atendeHorario() {
		LocalTime now = LocalTime.now();
		if(this.openingTime.isAfter(this.closingTime)) {
			return !now.isBefore(this.openingTime)  || !now.isAfter(this.closingTime);
		} else {
			return !now.isBefore(this.openingTime)  && !now.isAfter(this.closingTime);
		}
	}

	/** Quiosque aberto agora (dentro do horário de funcionamento). */
	public boolean estaAberto() {
		return atendeHorario();
	}

	/**
	 * Indica se é possível fazer um pedido com entrega para a posição do usuário
	 * usada na busca "nearby". Requer que o quiosque:
	 * <ul>
	 *   <li>esteja <b>aberto</b> agora ({@link #estaAberto()}) — não se pede a
	 *       um quiosque fechado;</li>
	 *   <li>faça entrega ({@code distAtendimento > 0});</li>
	 *   <li>e que o usuário esteja dentro do raio de atendimento
	 *       ({@code distancia <= distAtendimento}, com a {@code distancia} já
	 *       calculada e gravada na entidade durante a consulta).</li>
	 * </ul>
	 */
	public boolean podeEntregar() {
		return estaAberto()
				&& this.distAtendimento != null
				&& this.distAtendimento > 0
				&& this.distancia != null
				&& this.distancia <= this.distAtendimento;
	}
	
	public void atualizarDados(QuiosqueUpdateDTO quiosque) {
		if (quiosque.nome() != null) {
		    this.nome = quiosque.nome();
		}
		
		if (quiosque.openingTime() != null) {
			this.openingTime = quiosque.openingTime() ;
		}
		
		if (quiosque.closingTime() != null) {
			this.closingTime = quiosque.closingTime();
		}
		
		if (quiosque.distAtendimento() != null) {
			this.distAtendimento = quiosque.distAtendimento();
		}

		if (quiosque.cep() != null) {
		    this.endereco.setCep(quiosque.cep());
		}

		if (quiosque.uf() != null) {
			this.endereco.setUf(quiosque.uf());
		}

		if (quiosque.cidade() != null) {
			this.endereco.setCidade(quiosque.cidade());
		}
		
		if (quiosque.latitude() != null) {
		    this.coordenada.setLatitude(quiosque.latitude());
		}

		if (quiosque.longitude() != null) {
			this.coordenada.setLongitude(quiosque.longitude());
		}
	}
	
	public void addAvaliacao(long nota) {
	    avaliacaoResumo.setSomaNota(avaliacaoResumo.getSomaNota() + nota);
	    avaliacaoResumo.setTotalAvaliacoes(
	        avaliacaoResumo.getTotalAvaliacoes() + 1
	    );
	}
	
	public Long calcularTempoEstimado() {
	    Long somaTempo = somaTempoEntrega;
	    Long qtdPedidos = qtdPedidosFinalizados;

	    if (somaTempo != null && qtdPedidos != null && qtdPedidos > 0) {
	       return (somaTempo / qtdPedidos) + 10;
	    }
	    
	    return 0L;
	}
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public LocalTime getOpeningTime() {
		return openingTime;
	}

	public void setOpeningTime(LocalTime openingTime) {
		this.openingTime = openingTime;
	}

	public LocalTime getClosingTime() {
		return closingTime;
	}

	public void setClosingTime(LocalTime closingTime) {
		this.closingTime = closingTime;
	}

	public Long getDistAtendimento() {
		return distAtendimento;
	}

	public void setDistAtendimento(Long distAtendimento) {
		this.distAtendimento = distAtendimento;
	}

	public Long getSomaTempoEntrega() {
		return somaTempoEntrega;
	}

	public void setSomaTempoEntrega(Long somaTempoEntrega) {
		this.somaTempoEntrega = somaTempoEntrega;
	}

	public Long getQtdPedidosFinalizados() {
		return qtdPedidosFinalizados;
	}

	public void setQtdPedidosFinalizados(Long qtdPedidosFinalizados) {
		this.qtdPedidosFinalizados = qtdPedidosFinalizados;
	}

	public Long getDistancia() {
		return distancia;
	}

	public void setDistancia(Long distancia) {
		this.distancia = distancia;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public AvaliacaoResumo getAvaliacaoResumo() {
		return avaliacaoResumo;
	}

	public void setAvaliacaoResumo(AvaliacaoResumo avaliacaoResumo) {
		this.avaliacaoResumo = avaliacaoResumo;
	}

	public Coordenada getCoordenada() {
		return coordenada;
	}

	public void setCoordenada(Coordenada coordenada) {
		this.coordenada = coordenada;
	}

	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	public Usuario getProprietario() {
		return proprietario;
	}

	public void setProprietario(Usuario proprietario) {
		this.proprietario = proprietario;
	}

	public List<Usuario> getFuncionarios() {
		return funcionarios;
	}

	public void setFuncionarios(List<Usuario> funcionarios) {
		this.funcionarios = funcionarios;
	}

	public List<Acompanhamento> getAcompanhamentos() {
		return acompanhamentos;
	}

	public void setAcompanhamentos(List<Acompanhamento> acompanhamentos) {
		this.acompanhamentos = acompanhamentos;
	}

	public List<Pedido> getPedidos() {
		return pedidos;
	}

	public void setPedidos(List<Pedido> pedidos) {
		this.pedidos = pedidos;
	}

	public List<Categoria> getCategorias() {
		return categorias;
	}

	public void setCategorias(List<Categoria> categorias) {
		this.categorias = categorias;
	}

	public StatusConta getStatus() {
		return status;
	}

	public void setStatus(StatusConta status) {
		this.status = status;
	}

	public Quiosque(Long id, String nome, String email, String cnpj, @NotNull LocalTime openingTime,
			@NotNull LocalTime closingTime, @NotNull Long distAtendimento, Long somaTempoEntrega,
			Long qtdPedidosFinalizados, Long distancia, Endereco endereco, AvaliacaoResumo avaliacaoResumo,
			Coordenada coordenada, Imagem imagem, Usuario proprietario, List<Usuario> funcionarios,
			List<Acompanhamento> acompanhamentos, List<Pedido> pedidos, List<Categoria> categorias,
			StatusConta status) {
		super();
		this.id = id;
		this.nome = nome;
		this.email = email;
		this.cnpj = cnpj;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
		this.distAtendimento = distAtendimento;
		this.somaTempoEntrega = somaTempoEntrega;
		this.qtdPedidosFinalizados = qtdPedidosFinalizados;
		this.distancia = distancia;
		this.endereco = endereco;
		this.avaliacaoResumo = avaliacaoResumo;
		this.coordenada = coordenada;
		this.imagem = imagem;
		this.proprietario = proprietario;
		this.funcionarios = funcionarios;
		this.acompanhamentos = acompanhamentos;
		this.pedidos = pedidos;
		this.categorias = categorias;
		this.status = status;
	}

	public Quiosque() {
		super();
	}
	
}
