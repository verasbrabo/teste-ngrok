package app.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.service.CupomService;

@Component
public class CupomExpirationScheduler {

	private final CupomService cupomService;

	public CupomExpirationScheduler(CupomService cupomService) {
		this.cupomService = cupomService;
	}

	/** Verifica a cada minuto se algum cupom atingiu a data de expiracao. */
	@Scheduled(cron = "0 * * * * *")
	public void desativarCuponsExpirados() {
		cupomService.desativarCuponsExpirados();
	}
}
