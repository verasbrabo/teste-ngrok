package app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
//import java.util.Collection;
//import java.util.List;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import app.DTO.UpdateMeDTO;
import app.auth.DTO.RegisterDTO;
import app.enums.StatusConta;
import app.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
//import jakarta.persistence.PrePersist;
//import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(unique = true)
	private String deviceToken;
	@Column(nullable = false)
	private String nome;
	@Column(nullable = false)
	private String senhaHash;
	// Senha do funcionário cifrada (AES, reversível), para reexibição ao gestor.
	// Distinta do senhaHash (BCrypt, usado na autenticação). Nula para contas
	// cuja senha é definida pelo próprio usuário (ex.: proprietário/cliente).
	@Column(length = 512)
	private String senhaCifrada;
	private String cpf;
	@Column(nullable = false, unique = true)
	private String telefone;
	@Column(nullable = false, unique = true, updatable = false)
	@Email
	private String email;
	
	@Enumerated(EnumType.STRING)
	private UserRole role;	
	
	private String codigoEntrega;
	private LocalDate dataCadastro;
	private LocalDateTime ultimoLogin;
	
	@OneToOne
	@JoinColumn(name = "imagem_id")
	private Imagem imagem;
	
	@Enumerated(EnumType.STRING)
	private StatusConta status;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "quiosque_id", nullable = true)
	private Quiosque quiosque;
	
	@PrePersist
	protected void onCreate() {
		if(publicId == null)
			publicId = UUID.randomUUID();
	    this.dataCadastro = LocalDate.now();
	    this.status = StatusConta.Ativa;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if(this.role == UserRole.PROPRIETARIO) return List.of(new SimpleGrantedAuthority("ROLE_PROPRIETARIO"), new SimpleGrantedAuthority("ROLE_GERENTE"), new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
		if(this.role == UserRole.GERENTE) return List.of(new SimpleGrantedAuthority("ROLE_GERENTE"), new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
		if(this.role == UserRole.FUNCIONARIO) return List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
		return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
	}

	@Override
	public @Nullable String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	public Usuario(String nome, String email, String senhaHash, String cpf, String telefone, UserRole role) {
		super();
		this.nome = nome;
		this.email = email;
		this.senhaHash = senhaHash;
		this.cpf = cpf;
		this.telefone = telefone;
		this.role = role;
	}

	public Usuario(Long id, String nome, String senhaHash, String cpf, String telefone, @Email String email,
			UserRole role, String codigoEntrega, LocalDate dataCadastro, LocalDateTime ultimoLogin,
			Imagem imagem, StatusConta status, Quiosque quiosque) {
		super();
		this.id = id;
		this.nome = nome;
		this.senhaHash = senhaHash;
		this.cpf = cpf;
		this.telefone = telefone;
		this.email = email;
		this.role = role;
		this.codigoEntrega = codigoEntrega;
		this.dataCadastro = dataCadastro;
		this.ultimoLogin = ultimoLogin;
		this.imagem = imagem;
		this.status = status;
		this.quiosque = quiosque;
	}
	
	public void atualizarDados(RegisterDTO user) {
		if (user.nome() != null && !user.nome().isBlank()) {
		    this.setNome(user.nome());
		}

		if (user.cpf() != null && !user.cpf().isBlank()) {
			this.setCpf(user.cpf());
		}

		if (user.telefone() != null && !user.telefone().isBlank()) {
			this.setTelefone(user.telefone());
		}
	}

	/** Atualização de perfil (PUT /me): só nome/email/telefone. */
	public void atualizarPerfil(UpdateMeDTO data) {
		if (data.nome() != null && !data.nome().isBlank()) {
			this.setNome(data.nome());
		}

		if (data.email() != null && !data.email().isBlank()) {
			this.setEmail(data.email());
		}

		if (data.telefone() != null && !data.telefone().isBlank()) {
			this.setTelefone(data.telefone());
		}
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

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public String getSenhaCifrada() {
		return senhaCifrada;
	}

	public void setSenhaCifrada(String senhaCifrada) {
		this.senhaCifrada = senhaCifrada;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getCodigoEntrega() {
		return codigoEntrega;
	}

	public void setCodigoEntrega(String codigoEntrega) {
		this.codigoEntrega = codigoEntrega;
	}

	public LocalDate getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDate dataCadastro) {
		this.dataCadastro = dataCadastro;
	}


	public LocalDateTime getUltimoLogin() {
		return ultimoLogin;
	}

	public void setUltimoLogin(LocalDateTime ultimoLogin) {
		this.ultimoLogin = ultimoLogin;
	}

	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	public StatusConta getStatus() {
		return status;
	}

	public void setStatus(StatusConta status) {
		this.status = status;
	}

	public Quiosque getQuiosque() {
		return quiosque;
	}

	public void setQuiosque(Quiosque quiosque) {
		this.quiosque = quiosque;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public UUID getPublicId() {
		return publicId;
	}

	public void setPublicId(UUID publicId) {
		this.publicId = publicId;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public Usuario() {
		super();
	}
		    
}
