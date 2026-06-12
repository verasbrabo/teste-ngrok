package app.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import app.DTO.ItemCreateRequest;
import app.DTO.ItemDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ordem", "id_categoria"}))
public class Item {	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	// `ordem` é opcional (o app não a envia); `Long` evita o NPE de unboxing
	// que derrubava a criação de itens. Nulos não violam a unique (ordem,
	// id_categoria) no MySQL.
	public Item(String nome, String tipo, String descricao, BigDecimal valorBase,BigDecimal valorPromo, Categoria categoria, Long ordem) {
		super();
		this.nome = nome;
		this.tipo = tipo;
		this.descricao = descricao;
		this.valorBase = valorBase;
		this.valorPromo = valorPromo;
		this.categoria = categoria;
		this.ordem = ordem;
	}
	private String nome, tipo, descricao;
	private BigDecimal valorBase, valorPromo;
	private Long ordem;

	// Soft delete: "excluir" marca ativo=false (o item some do cardápio, mas
	// continua referenciado pelo histórico de pedidos em item_pedido).
	private boolean ativo = true;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemIngrediente> ingredientes = new ArrayList<>();
	
	@OneToOne
	@JoinColumn(name = "imagem_id")
	private Imagem imagem;
	@ManyToOne
	@JoinColumn(name="idCategoria", nullable = false)
	private Categoria categoria;	
	
	@OneToMany(mappedBy = "item")
	private List<ItemPedido> itemPedido ;
	
	@ManyToMany
	@JoinTable(
		        name = "item_acompanhamento",
		        joinColumns = @JoinColumn(name = "item_id"),
		        inverseJoinColumns = @JoinColumn(name = "acompanhamento_id")
		      )
    private List<Acompanhamento> acompanhamentos;
	
	public static Item from(ItemCreateRequest data, Categoria c) {
		return new Item(
		        data.nome(),
		        data.tipo(),
		        data.descricao(),
		        data.valorBase(),
		        data.valorPromo(),
		        c,
		        data.ordem()
		    );
	}
	
	public BigDecimal getValorFinal() {
	    if (valorPromo != null) {
	        return valorPromo;
	    }
	    return valorBase;
	}
	
	public void addIngrediente(Ingrediente ingrediente) {
		ingredientes.add(new ItemIngrediente(this, ingrediente));
	}
	
	public void atualizar(ItemCreateRequest data) {
		this.setNome(data.nome());
		this.setTipo(data.tipo());
		this.setDescricao(data.descricao());
		this.setValorBase(data.valorBase());
		this.setValorPromo(data.valorPromo());
		// Mantém a ordem atual quando o corpo não a informa.
		if (data.ordem() != null) this.setOrdem(data.ordem());
	}
	
	public boolean pertenceAoQuiosque(Quiosque quiosque) {

	    return this.categoria != null
	            && this.categoria.getQuiosque() != null
	            && this.categoria.getQuiosque().getId().equals(quiosque.getId());
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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getValorBase() {
		return valorBase;
	}

	public void setValorBase(BigDecimal valorBase) {
		this.valorBase = valorBase;
	}

	public BigDecimal getValorPromo() {
		return valorPromo;
	}

	public void setValorPromo(BigDecimal valorPromo) {
		this.valorPromo = valorPromo;
	}

	public Long getOrdem() {
		return ordem;
	}

	public void setOrdem(Long ordem) {
		this.ordem = ordem;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public List<ItemIngrediente> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<ItemIngrediente> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public List<ItemPedido> getItemPedido() {
		return itemPedido;
	}

	public void setItemPedido(List<ItemPedido> itemPedido) {
		this.itemPedido = itemPedido;
	}

	public List<Acompanhamento> getAcompanhamentos() {
		return acompanhamentos;
	}

	public void setAcompanhamentos(List<Acompanhamento> acompanhamentos) {
		this.acompanhamentos = acompanhamentos;
	}

	public Item(Long id, String nome, String tipo, String descricao, BigDecimal valorBase, BigDecimal valorPromo,
			Long ordem, List<ItemIngrediente> ingredientes, Imagem imagem, Categoria categoria,
			List<ItemPedido> itemPedido, List<Acompanhamento> acompanhamentos) {
		super();
		this.id = id;
		this.nome = nome;
		this.tipo = tipo;
		this.descricao = descricao;
		this.valorBase = valorBase;
		this.valorPromo = valorPromo;
		this.ordem = ordem;
		this.ingredientes = ingredientes;
		this.imagem = imagem;
		this.categoria = categoria;
		this.itemPedido = itemPedido;
		this.acompanhamentos = acompanhamentos;
	}

	public Item() {
		super();
	}
	
	
}