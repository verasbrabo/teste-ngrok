package app.DTO;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DailyRevenuePointDTO(
        OffsetDateTime date,
        BigDecimal value) {
}
