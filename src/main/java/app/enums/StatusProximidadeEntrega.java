package app.enums;

/**
 * Faixas estabilizadas de proximidade calculadas pelo app do quiosque.
 */
public enum StatusProximidadeEntrega {
	FORA_DE_ALCANCE("Entregador fora de alcance."),
	SINAL_FRACO("Seu entregador foi detectado."),
	APROXIMANDO("Seu entregador está se aproximando."),
	MUITO_PROXIMO("Seu entregador está muito próximo."),
	NO_LOCAL("Seu entregador chegou ao local da entrega.");

	private final String mensagem;

	StatusProximidadeEntrega(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getMensagem() {
		return mensagem;
	}
}
