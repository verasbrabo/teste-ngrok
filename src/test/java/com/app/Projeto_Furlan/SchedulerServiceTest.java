package app.scheduler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.auth.AuthService;
import app.service.CupomService;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

	@Mock
	private CupomService cupomService;
	@Mock
	private AuthService authService;
	@InjectMocks
	private RecoveryTokenDeleteScheduler recoveryTokenDeleteScheduler;

	@Test
	void deveDelegarExpiracaoDeCupons() {
		new CupomExpirationScheduler(cupomService).desativarCuponsExpirados();

		verify(cupomService).desativarCuponsExpirados();
	}

	@Test
	void deveDelegarLimpezaDeTokens() {
		recoveryTokenDeleteScheduler.deletarTokensObsoletos();

		verify(authService).deletarTokensObsoletos();
	}
}
