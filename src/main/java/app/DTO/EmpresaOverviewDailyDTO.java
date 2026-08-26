package app.DTO;

public record EmpresaOverviewDailyDTO(
		Long pedidosHoje,
		Double varPedidos,
		Double faturamentoHoje,
		Double varFaturamento,
		Double ticketMedio,
		Double varTicket,
		Double tempoMedio,
		Double varTempo
		) {

}
