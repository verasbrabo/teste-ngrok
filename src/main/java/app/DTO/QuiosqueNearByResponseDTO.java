package app.DTO;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import app.entity.Categoria;
import app.entity.Quiosque;
import app.enums.Categorias;

public record QuiosqueNearByResponseDTO(
	    Long id,
	    String nome,
	    Long distancia,
	    Long distAtendimento,
	    Long tempoEstimado,
	    List<Categorias> categorias,
	    BigDecimal nota,
	    Long qtdAvaliacoes,
	    String imagem,
	    String openingTime,
	    String closingTime,
	    boolean aberto,
	    boolean disponivelEntrega
	) {
	    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

	    public static QuiosqueNearByResponseDTO from(Quiosque q) {
	        return new QuiosqueNearByResponseDTO(
	            q.getId(),
	            q.getNome(),
	            q.getDistancia(),
	            q.getDistAtendimento(),
	            q.calcularTempoEstimado(),
	            q.getCategorias().stream().filter(Categoria::isAtivo).map(Categoria::getNome).toList(),
	            q.getAvaliacaoResumo() != null ? q.getAvaliacaoResumo().getMedia() : null,
	            q.getAvaliacaoResumo() != null ? q.getAvaliacaoResumo().getTotalAvaliacoes() : 0L,
	            q.getImagem() != null ? q.getImagem().getUrl() : null,
	            q.getOpeningTime() != null ? q.getOpeningTime().format(HORA) : null,
	            q.getClosingTime() != null ? q.getClosingTime().format(HORA) : null,
	            q.estaAberto(),
	            q.podeEntregar()
	        );
	    }
	}
