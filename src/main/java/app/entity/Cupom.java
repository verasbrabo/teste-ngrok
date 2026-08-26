package app.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.annotations.NotNull;

import app.enums.TipoCupom;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoCupom tipoCupom;

    @ManyToOne
    @JoinColumn(name="id_quiosque", nullable = false)
    private Quiosque quiosque;
    
    @OneToMany(
    	    mappedBy = "cupom",
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true
    	)
    	private List<CupomCliente> clientes = new ArrayList<>();
    
    
    
    
    
    public List<CupomCliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<CupomCliente> clientes) {
		this.clientes = clientes;
	}

	private BigDecimal valorDesconto;

    private BigDecimal valorMinimo;

    private BigDecimal descontoMaximo;
    
    private LocalDateTime dataExpiracao;

    private boolean ativo;
    
    public BigDecimal calcularDesconto(BigDecimal subTotal) {
    	BigDecimal desconto;
    	if(this.podeSerAplicado(subTotal)) {
    		
    		if (TipoCupom.FIXO.equals(tipoCupom)) {
    			desconto = valorDesconto.min(subTotal);
    		} else {
    			desconto = subTotal.multiply(this.valorDesconto).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    		}
    		
    		return desconto.min(descontoMaximo);
    		
    	}
    	return BigDecimal.ZERO;
    }
    
    public boolean podeSerAplicado(BigDecimal subTotal) {

        LocalDateTime agora = LocalDateTime.now();

        return this.isAtivo()
                && this.getValorMinimo().compareTo(subTotal) <= 0
                && this.getDataExpiracao().isAfter(agora);
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoCupom getTipoCupom() {
		return tipoCupom;
	}

	public void setTipoCupom(TipoCupom tipoCupom) {
		this.tipoCupom = tipoCupom;
	}

	public Quiosque getQuiosque() {
		return quiosque;
	}

	public void setQuiosque(Quiosque quiosque) {
		this.quiosque = quiosque;
	}

	public BigDecimal getValorDesconto() {
		return valorDesconto;
	}

	public void setValorDesconto(BigDecimal valorDesconto) {
		this.valorDesconto = valorDesconto;
	}

	public BigDecimal getValorMinimo() {
		return valorMinimo;
	}

	public void setValorMinimo(BigDecimal valorMinimo) {
		this.valorMinimo = valorMinimo;
	}

	public BigDecimal getDescontoMaximo() {
		return descontoMaximo;
	}

	public void setDescontoMaximo(BigDecimal descontoMaximo) {
		this.descontoMaximo = descontoMaximo;
	}

	public LocalDateTime getDataExpiracao() {
		return dataExpiracao;
	}

	public void setDataExpiracao(LocalDateTime dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Cupom(TipoCupom tipoCupom, Quiosque quiosque, BigDecimal valorDesconto, BigDecimal valorMinimo,
			BigDecimal descontoMaximo, LocalDateTime dataExpiracao, boolean ativo) {
		super();
		this.tipoCupom = tipoCupom;
		this.quiosque = quiosque;
		this.valorDesconto = valorDesconto;
		this.valorMinimo = valorMinimo;
		this.descontoMaximo = descontoMaximo;
		this.dataExpiracao = dataExpiracao;
		this.ativo = ativo;
	}

	public Cupom() {
		super();
	}
    
    
}
