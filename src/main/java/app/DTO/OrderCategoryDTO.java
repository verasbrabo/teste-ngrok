package app.DTO;

public record OrderCategoryDTO(
        Long categoryId,
        String categoryName,
        Integer orderCount) {
}
