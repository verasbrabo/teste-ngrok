package app.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.auth.AuthService;

@Component
public class RecoveryTokenDeleteScheduler {
	
	@Autowired
	AuthService authService;
	
	/** Verifica a cada 30 minuto se algum cupom atingiu a data de expiracao. */
	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
	public void deletarTokensObsoletos() {
		authService.deletarTokensObsoletos();
	}

}
