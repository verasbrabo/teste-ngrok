package app.DTO;

import java.math.BigDecimal;
import java.util.List;

public record KioskRevenueDTO(
        Long kioskId,
        String kioskName,
        List<BigDecimal> revenue) {
}
