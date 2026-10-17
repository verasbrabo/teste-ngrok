package app.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {
	
	@Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String destinatario, String assunto, String texto) {

        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText("Seu código de recuperação é: " + texto);

        System.out.println(mensagem.toString());
        mailSender.send(mensagem);
    }

	public void enviarCredenciaisFuncionario(
			String destinatario,
			String login,
			String senha) {
		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setTo(destinatario);
		mensagem.setSubject("Seus dados de acesso");
		mensagem.setText("""
				Olá,

				Seu acesso foi criado com sucesso.

				Login: %s
				Senha provisória: %s

				Por segurança, altere a senha no primeiro acesso.
				""".formatted(login, senha));
		mailSender.send(mensagem);
	}
}

