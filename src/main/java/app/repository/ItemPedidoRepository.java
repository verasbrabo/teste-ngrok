package app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.entity.ItemPedido;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long>{
	
	@EntityGraph(attributePaths = {"item"})
	List<ItemPedido> findByPedidoIdIn(List<UUID> ids);
	
	@Query("""
		    select distinct ip
		    from ItemPedido ip
		    left join fetch ip.item
		    left join fetch ip.acompanhamentos
		    where ip.pedido.id in :pedidoIds
		""")
		List<ItemPedido> findByPedidoIdInWithDetails(
		    @Param("pedidoIds") List<UUID> pedidoIds
		);

	@Query("""
			SELECT
				i.id AS productId,
				i.nome AS productName,
				SUM(ip.quantidade) AS unitsSold
			FROM ItemPedido ip
			JOIN ip.item i
			JOIN ip.pedido p
			JOIN p.quiosque q
			WHERE q.proprietario.id = :proprietarioId
			  AND (:kioskId IS NULL OR q.id = :kioskId)
			  AND p.status IN (app.enums.StatusPedido.FINALIZADO, app.enums.StatusPedido.AVALIADO)
			  AND p.dataHoraPedido >= :inicio
			  AND p.dataHoraPedido < :fim
			GROUP BY i.id, i.nome
			ORDER BY SUM(ip.quantidade) DESC
			""")
	List<ProdutoMaisVendidoProjection> buscarProdutosMaisVendidos(
			@Param("proprietarioId") Long proprietarioId,
			@Param("kioskId") Long kioskId,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
			Pageable pageable);

	@Query("""
			SELECT
				c.id AS categoryId,
				c.nome AS categoryName,
				COUNT(DISTINCT p.id) AS orderCount
			FROM ItemPedido ip
			JOIN ip.item i
			JOIN i.categoria c
			JOIN ip.pedido p
			JOIN p.quiosque q
			WHERE q.proprietario.id = :proprietarioId
			  AND (:kioskId IS NULL OR q.id = :kioskId)
			  AND p.status IN (app.enums.StatusPedido.FINALIZADO, app.enums.StatusPedido.AVALIADO)
			  AND p.dataHoraPedido >= :inicio
			  AND p.dataHoraPedido < :fim
			GROUP BY c.id, c.nome
			ORDER BY COUNT(DISTINCT p.id) DESC
			""")
	List<PedidosPorCategoriaProjection> buscarPedidosPorCategoria(
			@Param("proprietarioId") Long proprietarioId,
			@Param("kioskId") Long kioskId,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim);

}
