package app.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FaturamentoPorQuiosqueProjection {
    Long getKioskId();

    String getKioskName();

    LocalDate getData();

    BigDecimal getFaturamento();
}
