package app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RecoveryToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String token;
	
	@ManyToOne
	@JoinColumn(name = "id_usuario")
	private Usuario usuario;
	
	private LocalDateTime expiracao;
	
	private LocalDateTime enviado;

	public RecoveryToken(Long id, String token, Usuario usuario, LocalDateTime expiracao, LocalDateTime ultimoEnviado) {
		super();
		this.id = id;
		this.token = token;
		this.usuario = usuario;
		this.expiracao = expiracao;
		this.enviado = ultimoEnviado;
	}

	public RecoveryToken() {
		super();
	}

	public RecoveryToken(String token, Usuario usuario, LocalDateTime expiracao, LocalDateTime ultimoEnviado) {
		super();
		this.token = token;
		this.usuario = usuario;
		this.expiracao = expiracao;
		this.enviado = ultimoEnviado;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getExpiracao() {
		return expiracao;
	}

	public void setExpiracao(LocalDateTime expiracao) {
		this.expiracao = expiracao;
	}

	public LocalDateTime getEnviado() {
		return enviado;
	}

	public void setEnviado(LocalDateTime ultimoEnviado) {
		this.enviado = ultimoEnviado;
	}
	
	
}
