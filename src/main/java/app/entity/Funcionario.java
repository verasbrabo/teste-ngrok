package app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

	@Column(unique = true, length = 14)
	private String cnpj;
	
	private LocalTime inicioTurno;
	
	private LocalTime fimTurno;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id_quiosque", nullable = true)
	private Quiosque quiosque;
	
	@OneToMany(mappedBy = "proprietario")
	private List<Quiosque> quiosques;

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
