package app.DTO;

import java.time.LocalDate;
import java.util.List;

public record RevenueByKioskDTO(
        List<LocalDate> dates,
        List<KioskRevenueDTO> kiosks) {
}
