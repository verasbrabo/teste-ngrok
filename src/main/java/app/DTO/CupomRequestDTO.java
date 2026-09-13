package app.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import app.enums.TipoCupom;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

/** Dados aceitos no cadastro e na edição de um cupom. */
public record CupomRequestDTO(
		@NotNull(message = "Informe o tipo do cupom")
		TipoCupom tipoCupom,

		@NotNull(message = "Informe o valor do desconto")
		@DecimalMin(value = "0.01", message = "O desconto deve ser maior que zero")
		BigDecimal valorDesconto,

		@NotNull(message = "Informe o valor mínimo do pedido")
		@DecimalMin(value = "0.01", message = "O valor mínimo deve ser maior que zero")
		BigDecimal valorMinimo,

		@DecimalMin(value = "0.01", message = "O limite do desconto deve ser maior que zero")
		BigDecimal descontoMaximo,

		@NotNull(message = "Informe a validade do cupom")
		@Future(message = "A validade deve estar no futuro")
		LocalDateTime dataExpiracao
) {
}
