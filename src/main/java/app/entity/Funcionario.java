package app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import app.enums.StatusConta;
import app.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;

@Entity
public class Funcionario extends Usuario{
	private static final int TODOS_DIAS_ATIVOS = 0b1111111;

	@Column(unique = true, length = 14)
	private String cnpj;
	
	private LocalTime inicioTurno;
	
	private LocalTime fimTurno;

	// Ordem dos bits/lista: domingo (0) até sábado (6).
	@Column(name = "dias_turno_bitmask")
	private Integer diasTurnoBitmask = TODOS_DIAS_ATIVOS;

	// Contas antigas e proprietários não são obrigados a trocar a senha.
	// A flag é ativada explicitamente apenas ao gerar uma senha provisória.
	@Column(name = "deve_trocar_senha")
	private Boolean deveTrocarSenha = false;

	@Column(length = 254)
	private String email;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id_quiosque", nullable = true)
	private Quiosque quiosque;
	
	@OneToMany(mappedBy = "proprietario")
	private List<Quiosque> quiosques;

	public List<Boolean> getDiasTurnoLista() {
		int dias = diasTurnoBitmask == null ? TODOS_DIAS_ATIVOS : diasTurnoBitmask;
		return IntStream.range(0, 7)
				.mapToObj(indice -> (dias & (1 << indice)) != 0)
				.toList();
	}

	public void setDiasTurno(List<Boolean> diasTurno) {
		if (diasTurno == null) {
			return;
		}
		if (diasTurno.size() != 7) {
			throw new IllegalArgumentException("Os dias de trabalho devem conter sete valores");
		}

		this.diasTurnoBitmask = IntStream.range(0, 7)
				.filter(indice -> Boolean.TRUE.equals(diasTurno.get(indice)))
				.map(indice -> 1 << indice)
				.sum();
	}

	public Funcionario() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Funcionario(Long id, String nome, String senhaHash, String cpf, String telefone, @Email String email,
			UserRole role, LocalDate dataCadastro, LocalDateTime ultimoLogin, Imagem imagem, StatusConta status) {
		super(id, nome, senhaHash, cpf, telefone, email, role, dataCadastro, ultimoLogin, imagem, status);
		// TODO Auto-generated constructor stub
	}

	public Funcionario(String nome, String email, String senhaHash, String cpf, String telefone, UserRole role) {
		super(nome, email, senhaHash, cpf, telefone, role);
		// TODO Auto-generated constructor stub
	}

	public LocalTime getInicioTurno() {
		return inicioTurno;
	}

	public void setInicioTurno(LocalTime inicioTurno) {
		this.inicioTurno = inicioTurno;
	}

	public LocalTime getFimTurno() {
		return fimTurno;
	}

	public void setFimTurno(LocalTime fimTurno) {
		this.fimTurno = fimTurno;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public Integer getDiasTurnoBitmask() {
		return diasTurnoBitmask;
	}

	public void setDiasTurnoBitmask(Integer diasTurnoBitmask) {
		this.diasTurnoBitmask = diasTurnoBitmask;
	}

	public Boolean getDeveTrocarSenha() {
		return deveTrocarSenha;
	}

	public void setDeveTrocarSenha(Boolean deveTrocarSenha) {
		this.deveTrocarSenha = deveTrocarSenha;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Quiosque getQuiosque() {
		return quiosque;
	}

	public void setQuiosque(Quiosque quiosque) {
		this.quiosque = quiosque;
	}

	public List<Quiosque> getQuiosques() {
		return quiosques;
	}

	public void setQuiosques(List<Quiosque> quiosques) {
		this.quiosques = quiosques;
	}

	


}
