package app.repository;

import java.util.List;
import java.util.UUID;

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

}
