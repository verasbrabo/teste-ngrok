package app.DTO;

import app.enums.StatusProximidadeEntrega;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Corpo usado pelo quiosque para publicar RSSI medio de aproximacao.
// O valor deve ser negativo em dBm; quanto mais perto de zero, mais forte.
public record AtualizarProximidadeDTO(
        @NotNull
        @Min(-120)
        @Max(0)
        Integer rssiMedio,
        @NotNull
        StatusProximidadeEntrega statusProximidade
) {

}
