package app.DTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

import app.entity.Categoria;
import app.entity.Quiosque;
import app.enums.Categorias;
import app.repository.QuiosqueMetricasProjection;

public record QuiosqueOverviewDTO(
		Long id,
		String nome,
		BigDecimal nota,
		Long pedidosHoje,
		Double varPedidos,
		
		BigDecimal faturamentoHoje,
		Double varFaturamento,
		
		BigDecimal ticketMedio,
		Double varTicket,
		
		Double tempoMedio,
		Double varTempo,
		String openingTime,
		String closingTime,
		Boolean aberto,
		List<String> categorias,
		String imagem,
		String cep,
		String uf,
		String cidade
		
		) {
	
	public static QuiosqueOverviewDTO from(
	        Quiosque q,
	        QuiosqueMetricasProjection m
	) {

	    DateTimeFormatter formatter =
	            DateTimeFormatter.ofPattern("HH:mm");

	    String openTime =
	            q.getOpeningTime().format(formatter);

	    String closeTime =
	            q.getClosingTime().format(formatter);

	    if (m == null) {
	        return new QuiosqueOverviewDTO(
	        		q.getId(),
	                q.getNome(),

	                q.getAvaliacaoResumo() != null
	                        ? q.getAvaliacaoResumo().getMedia()
	                        : null,

	                0L,
	                0.0,

	                BigDecimal.ZERO,
	                0.0,

	                BigDecimal.ZERO,
	                0.0,

	                0.0,
	                0.0,

	                openTime,
	                closeTime,
	                q.estaAberto(),

	                q.getCategorias() != null
	                        ? q.getCategorias()
	                            .stream()
	                            .map(Categoria::getNome).map(Categorias::getCategorias)
	                            .toList()
	                        : List.of(),

	                q.getImagem() != null
	                        ? q.getImagem().getUrl()
	                        : null,

	                q.getEndereco().getCep(),
	                q.getEndereco().getUf(),
	                q.getEndereco().getCidade()
	        );
	    }

	    Long pedidosHoje =
	            m.getPedidosHoje() != null
	                    ? m.getPedidosHoje()
	                    : 0L;

	    Long pedidosOntem =
	            m.getPedidosOntem() != null
	                    ? m.getPedidosOntem()
	                    : 0L;

	    BigDecimal faturamentoHoje =
	            m.getFaturamentoHoje() != null
	                    ? m.getFaturamentoHoje()
	                    : BigDecimal.ZERO;

	    BigDecimal faturamentoOntem =
	            m.getFaturamentoOntem() != null
	                    ? m.getFaturamentoOntem()
	                    : BigDecimal.ZERO;

	    BigDecimal ticketMedioHoje =
	            calcularTicket(
	                    faturamentoHoje,
	                    pedidosHoje
	            );

	    BigDecimal ticketMedioOntem =
	            calcularTicket(
	                    faturamentoOntem,
	                    pedidosOntem
	            );

	    Double tempoMedioHoje =
	            m.getTempoMedioHoje() != null
	                    ? m.getTempoMedioHoje()
	                    : 0.0;

	    Double tempoMedioOntem =
	            m.getTempoMedioOntem() != null
	                    ? m.getTempoMedioOntem()
	                    : 0.0;

	    return new QuiosqueOverviewDTO(
	    		q.getId(),

	            q.getNome(),

	            q.getAvaliacaoResumo() != null
	                    ? q.getAvaliacaoResumo().getMedia()
	                    : null,

	            pedidosHoje,

	            calcularVariacao(
	                    pedidosHoje.doubleValue(),
	                    pedidosOntem.doubleValue()
	            ),

	            faturamentoHoje,

	            calcularVariacao(
	                    faturamentoHoje,
	                    faturamentoOntem
	            ),

	            ticketMedioHoje,

	            calcularVariacao(
	                    ticketMedioHoje,
	                    ticketMedioOntem
	            ),

	            tempoMedioHoje,

	            calcularVariacao(
	                    tempoMedioHoje,
	                    tempoMedioOntem
	            ),

	            openTime,
	            closeTime,

	            q.estaAberto(),

	            q.getCategorias() != null
		                ? q.getCategorias()
		                    .stream()
		                    .map(Categoria::getNome).map(Categorias::getCategorias)
		                    .toList()
		                : List.of(),

	            q.getImagem() != null
	                    ? q.getImagem().getUrl()
	                    : null,

	            q.getEndereco().getCep(),
	            q.getEndereco().getUf(),
	            q.getEndereco().getCidade()
	    );
	}
	
	private static BigDecimal calcularTicket(
	        BigDecimal faturamento,
	        Long pedidos
	) {

	    if (pedidos == null || pedidos == 0) {
	        return BigDecimal.ZERO;
	    }

	    return faturamento.divide(
	            BigDecimal.valueOf(pedidos),
	            2,
	            RoundingMode.HALF_UP
	    );
	}
	
	private static Double calcularVariacao(
	        double atual,
	        double anterior
	) {

	    if (anterior == 0) {
	        if (atual == 0) {
	            return 0.0;
	        }

	        return 100.0;
	    }

	    return ((atual - anterior) / anterior) * 100.0;
	}
	
	private static Double calcularVariacao(
	        BigDecimal atual,
	        BigDecimal anterior
	) {

	    if (anterior == null ||
	        anterior.compareTo(BigDecimal.ZERO) == 0) {

	        if (atual == null ||
	            atual.compareTo(BigDecimal.ZERO) == 0) {

	            return 0.0;
	        }

	        return 100.0;
	    }

	    return atual
	            .subtract(anterior)
	            .divide(
	                    anterior,
	                    4,
	                    RoundingMode.HALF_UP
	            )
	            .multiply(BigDecimal.valueOf(100))
	            .doubleValue();
	}

}
