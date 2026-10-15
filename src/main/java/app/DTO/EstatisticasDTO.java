package app.DTO;

public record EstatisticasDTO(
        RevenueByKioskDTO revenueByKiosk,
        TopProductsDTO topProducts,
        DailyRevenueDTO dailyRevenue,
        OrdersByCategoryDTO ordersByCategory) {
}
