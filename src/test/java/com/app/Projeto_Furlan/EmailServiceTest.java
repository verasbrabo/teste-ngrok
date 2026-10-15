package app.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private EmailService emailService;

	@Test
	void deveMontarEmailDeRecuperacaoAntesDoEnvio() {
		emailService.enviarEmail("cliente@teste.com", "Recuperacao", "AB12");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		SimpleMailMessage mensagem = captor.getValue();

		assertArrayEquals(new String[] {"cliente@teste.com"}, mensagem.getTo());
		assertEquals("Recuperacao", mensagem.getSubject());
		assertEquals("Seu codigo de recuperacao e: AB12", removerAcentos(mensagem.getText()));
	}

	@Test
	void deveEnviarLoginESenhaProvisoriaAoFuncionario() {
		emailService.enviarCredenciaisFuncionario(
				"funcionario@teste.com", "QUI01funcionario", "Senha123");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		SimpleMailMessage mensagem = captor.getValue();

		assertArrayEquals(new String[] {"funcionario@teste.com"}, mensagem.getTo());
		assertEquals("Seus dados de acesso", mensagem.getSubject());
		assertTrue(mensagem.getText().contains("Usuário: QUI01funcionario"));
		assertTrue(mensagem.getText().contains("Senha provisória: Senha123"));
	}

	private String removerAcentos(String texto) {
		return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
	}
}
