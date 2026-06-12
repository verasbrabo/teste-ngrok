package app.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Ingrediente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
	@Column(nullable = false, unique = true)
	private String nome;
	
	@OneToMany(mappedBy = "ingrediente")
	private List<ItemIngrediente> itens;

	public Ingrediente(String nome) {
		super();
		this.nome = nome.trim().toLowerCase();
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

	public List<ItemIngrediente> getItens() {
		return itens;
	}

	public void setItens(List<ItemIngrediente> itens) {
		this.itens = itens;
	}

	public Ingrediente(Long id, String nome, List<ItemIngrediente> itens) {
		super();
		this.id = id;
		this.nome = nome;
		this.itens = itens;
	}

	public Ingrediente() {
		super();
	}
	
}
