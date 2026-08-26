package app.repository;

import java.math.BigDecimal;

public interface QuiosqueMetricasProjection {
	Long getQuiosqueId();

    Long getPedidosHoje();

    Long getPedidosOntem();

    BigDecimal getFaturamentoHoje();

    BigDecimal getFaturamentoOntem();

    Double getTempoMedioHoje();

    Double getTempoMedioOntem();
}
