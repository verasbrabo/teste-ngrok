package app.DTO;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Leitura de GPS publicada pelo aparelho que iniciou a entrega.
 */
public record LocalizacaoEntregaCreateDTO(
		@NotNull
		@DecimalMin("-90.0")
		@DecimalMax("90.0")
		Double latitude,
		@NotNull
		@DecimalMin("-180.0")
		@DecimalMax("180.0")
		Double longitude,
		@NotNull
		@Positive
		@DecimalMax("25.0")
		Double precisaoMetros,
		@PositiveOrZero
		@DecimalMax("360.0")
		Double direcaoGraus,
		@PositiveOrZero
		@DecimalMax("100.0")
		Double velocidadeMetrosSegundo,
		@NotNull
		Instant capturadoEm,
		@NotNull
		UUID idAparelhoEntregador) {
}
