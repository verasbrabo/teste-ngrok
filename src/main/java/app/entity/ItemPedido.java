package app.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class ItemPedido {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer quantidade;
	private BigDecimal valorUnit, valorAcomp, subTotal;
	
	@ManyToOne
	@JoinColumn(name="idItem")
	private Item item;
	
	@ManyToOne
	@JoinColumn(name="idPedido")
	private Pedido pedido;
	
	@ManyToMany
	private List<Acompanhamento> acompanhamentos = new ArrayList<>();
	
	@ManyToMany 
	private List<Ingrediente> ingredientesRemovido = new ArrayList<>();
	
	public void calcularSubtotal() {

	    BigDecimal adicionais = this.acompanhamentos.stream()
	            .map(Acompanhamento::getValor)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);
	    
	    this.setValorAcomp(adicionais);
	    
	    BigDecimal unitarioFinal = this.item.getValorFinal().add(adicionais);

	    this.subTotal = unitarioFinal.multiply(
	            BigDecimal.valueOf(this.quantidade)
	    );
	}

	public ItemPedido(Integer quantidade, BigDecimal valorUnit, Item item) {
		super();
		this.quantidade = quantidade;
		this.valorUnit = valorUnit;
		this.item = item;
	}
	
	public void adicionarAcompanhamentos(
	        List<Acompanhamento> acompanhamentos) {

	    this.acompanhamentos.addAll(acompanhamentos);
	}
	
	public void removerIngredientes(
			List<Ingrediente> ingredientes) {
		
		this.ingredientesRemovido.addAll(ingredientes);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValorUnit() {
		return valorUnit;
	}

	public void setValorUnit(BigDecimal valorUnit) {
		this.valorUnit = valorUnit;
	}

	public BigDecimal getValorAcomp() {
		return valorAcomp;
	}

	public void setValorAcomp(BigDecimal valorAcomp) {
		this.valorAcomp = valorAcomp;
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public List<Acompanhamento> getAcompanhamentos() {
		return acompanhamentos;
	}

	public void setAcompanhamentos(List<Acompanhamento> acompanhamentos) {
		this.acompanhamentos = acompanhamentos;
	}

	public List<Ingrediente> getIngredientesRemovido() {
		return ingredientesRemovido;
	}

	public void setIngredientesRemovido(List<Ingrediente> ingredientesRemovido) {
		this.ingredientesRemovido = ingredientesRemovido;
	}

	public ItemPedido(Long id, Integer quantidade, BigDecimal valorUnit, BigDecimal valorAcomp, BigDecimal subTotal,
			Item item, Pedido pedido, List<Acompanhamento> acompanhamentos, List<Ingrediente> ingredientesRemovido) {
		super();
		this.id = id;
		this.quantidade = quantidade;
		this.valorUnit = valorUnit;
		this.valorAcomp = valorAcomp;
		this.subTotal = subTotal;
		this.item = item;
		this.pedido = pedido;
		this.acompanhamentos = acompanhamentos;
		this.ingredientesRemovido = ingredientesRemovido;
	}

	public ItemPedido() {
		super();
	}
	
	
	
}
