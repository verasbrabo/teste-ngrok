package app.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class NotificationServiceTest {

	private final NotificationService notificationService = new NotificationService();

	@Test
	void deveIgnorarNotificacaoSemToken() {
		NotificationRequestDTO request = new NotificationRequestDTO(
				"  ", "Titulo", "Mensagem", "pedido-1", "PRONTO", null, "STATUS_PEDIDO");

		assertDoesNotThrow(() -> notificationService.sendNotificationToDevice(request));
	}
}
