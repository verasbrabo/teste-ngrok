package app.repository;

public interface ProdutoMaisVendidoProjection {
    Long getProductId();

    String getProductName();

    Long getUnitsSold();
}
