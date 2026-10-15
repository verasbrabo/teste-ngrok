package app.repository;

import app.enums.Categorias;

public interface PedidosPorCategoriaProjection {
    Long getCategoryId();

    Categorias getCategoryName();

    Long getOrderCount();
}
