package app.config;

import com.ngrok.Session;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Abre o tunel ngrok somente nos ambientes em que ele for habilitado.
 */
@Component
@ConditionalOnProperty(name = "ngrok.enabled", havingValue = "true")
public class NgrokConfiguration {

	private final String domain;
	private final int serverPort;
	private Session session;

	public NgrokConfiguration(
			@Value("${ngrok.domain:${NGROK_DOMAIN:stopped-crabbing-finalist.ngrok-free.dev}}") String domain,
			@Value("${server.port:8080}") int serverPort) {
		this.domain = domain;
		this.serverPort = serverPort;
	}

	/**
	 * Aguarda o servidor HTTP ficar pronto antes de encaminhar o dominio publico.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public synchronized void connect() throws Exception {
		if (session != null) {
			return;
		}

		session = Session.withAuthtokenFromEnv().connect();
		var localApi = URI.create("http://localhost:" + serverPort).toURL();
		var forwarder = session.httpEndpoint()
				.domain(domain)
				.forward(localApi);

		System.out.println("Ngrok disponivel em: " + forwarder.getUrl());
	}

	/**
	 * Encerra a sessao junto com a aplicacao para nao deixar recursos abertos.
	 */
	@PreDestroy
	public synchronized void disconnect() throws Exception {
		if (session != null) {
			session.close();
			session = null;
		}
	}
}
