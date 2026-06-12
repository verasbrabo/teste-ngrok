package app.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import app.enums.Categorias;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ordem", "id_quiosque"}))
public class Categoria {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private Categorias nome;
	private Integer ordem;

	// Soft delete: "excluir" marca ativo=false (a categoria some do cardápio,
	// mas seus itens continuam referenciados pelo histórico de pedidos).
	private boolean ativo = true;

	// perigoso esse cascade all, conferir se é isso msm
	@OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Item> itens = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name="idQuiosque", nullable = false)
	private Quiosque quiosque;		
	
	public void addItem(Item item) {
	    itens.add(item);
	    item.setCategoria(this);
	}

	public void removeItem(Item item) {
	    itens.remove(item);
	    item.setCategoria(null);
	}
	
	public Item getItem(Long itemId) {
	   Item item = this.itens.stream()
	        .filter(i -> itemId.equals(i.getId()))
	        .findFirst()
	        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
	   return item;
	}

	public Categoria(Categorias nome, Integer ordem) {
		super();
		this.nome = nome;
		this.ordem = ordem;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Categorias getNome() {
		return nome;
	}

	public void setNome(Categorias nome) {
		this.nome = nome;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public List<Item> getItens() {
		return itens;
	}

	public void setItens(List<Item> itens) {
		this.itens = itens;
	}

	public Quiosque getQuiosque() {
		return quiosque;
	}

	public void setQuiosque(Quiosque quiosque) {
		this.quiosque = quiosque;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Categoria(Long id, Categorias nome, Integer ordem, List<Item> itens, Quiosque quiosque) {
		super();
		this.id = id;
		this.nome = nome;
		this.ordem = ordem;
		this.itens = itens;
		this.quiosque = quiosque;
	}

	public Categoria() {
		super();
	}
	
	
}
