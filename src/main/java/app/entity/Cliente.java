package app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import app.enums.StatusConta;
import app.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;

@Entity
public class Cliente extends Usuario{

	private String codigoEntrega;
	
	@OneToMany(
		    mappedBy = "cliente",
		    cascade = CascadeType.ALL,
		    orphanRemoval = true
		)
		private List<CupomCliente> cupons = new ArrayList<>();
	
	
	
	
	public List<CupomCliente> getCupons() {
		return cupons;
	}

	public void setCupons(List<CupomCliente> cupons) {
		this.cupons = cupons;
	}

	public Cliente() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Cliente(Long id, String nome, String senhaHash, String cpf, String telefone, @Email String email,
			UserRole role, LocalDate dataCadastro, LocalDateTime ultimoLogin, Imagem imagem, StatusConta status) {
		super(id, nome, senhaHash, cpf, telefone, email, role, dataCadastro, ultimoLogin, imagem, status);
		// TODO Auto-generated constructor stub
	}

	public Cliente(String nome, String email, String senhaHash, String cpf, String telefone, UserRole role) {
		super(nome, email, senhaHash, cpf, telefone, role);
		// TODO Auto-generated constructor stub
	}

	public String getCodigoEntrega() {
		return codigoEntrega;
	}

	public void setCodigoEntrega(String codigoEntrega) {
		this.codigoEntrega = codigoEntrega;
	}
}
