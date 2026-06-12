package app.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Setter
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
@Embeddable
public class AvaliacaoResumo {
	private Long somaNota, totalAvaliacoes;
	
	public BigDecimal getMedia() {
	    if (somaNota == null || totalAvaliacoes == null || totalAvaliacoes == 0) {
	        return null;
	    }

	    return BigDecimal.valueOf(somaNota)
	            .divide(BigDecimal.valueOf(totalAvaliacoes), 2, RoundingMode.HALF_UP);
	}

	public Long getSomaNota() {
		return somaNota;
	}

	public void setSomaNota(Long somaNota) {
		this.somaNota = somaNota;
	}

	public Long getTotalAvaliacoes() {
		return totalAvaliacoes;
	}

	public void setTotalAvaliacoes(Long totalAvaliacoes) {
		this.totalAvaliacoes = totalAvaliacoes;
	}

	public AvaliacaoResumo(Long somaNota, Long totalAvaliacoes) {
		super();
		this.somaNota = somaNota;
		this.totalAvaliacoes = totalAvaliacoes;
	}

	public AvaliacaoResumo() {
		super();
	}
	
	
}
