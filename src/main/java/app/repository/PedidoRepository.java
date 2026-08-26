package app.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusPedido;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

	
//   Page<Pedido> findByClienteAndStatusNotInOrderByDataHoraPedido(Usuario cliente, List<StatusPedido> status, Pageable pageable);
//   
//   Page<Pedido> findByClienteAndStatusOrderByDataHoraPedido(Usuario cliente, StatusPedido status, Pageable pageable);
//   
//   Page<Pedido> findByClienteOrderByDataHoraPedido(Usuario cliente, Pageable pageable);
//
//   Page<Pedido> findByQuiosqueOrderByDataHoraPedido(Quiosque quiosque, Pageable pageable);
//   
//   Page<Pedido> findByQuiosqueAndStatusInOrderByDataHoraPedido(Quiosque quiosque, List<StatusPedido> status, Pageable pageable);
//   
//   Page<Pedido> findByQuiosqueAndStatusOrderByDataHoraPedido(Quiosque quiosque, StatusPedido status, Pageable pageable);
   
   List<Pedido> findByClienteAndStatusNotInOrderByDataHoraPedido(Usuario cliente, List<StatusPedido> status);
   
   List<Pedido> findByClienteAndStatusOrderByDataHoraPedido(Usuario cliente, StatusPedido status);
   
   List<Pedido> findByClienteOrderByDataHoraPedido(Usuario cliente);

   List<Pedido> findByQuiosqueOrderByDataHoraPedido(Quiosque quiosque);
   
   List<Pedido> findByQuiosqueAndStatusInOrderByDataHoraPedido(Quiosque quiosque, List<StatusPedido> status);
   
   List<Pedido> findByQuiosqueAndStatusOrderByDataHoraPedido(Quiosque quiosque, StatusPedido status);
   
   List<Pedido> findByQuiosqueIdIn(List<Long> ids); // Teste
   
   List<Pedido> findByQuiosqueIdInOrderByDataHoraPedidoDesc(List<Long> ids); // teste
   
   List<Pedido> findByQuiosqueIdOrderByDataHoraPedidoDesc(Long id);
   
   @EntityGraph(attributePaths = {"quiosque", "cliente"})
   Page<Pedido> findByQuiosqueIdIn(List<Long> ids, Pageable pageable); // Teste
	
   Page<Pedido> findByQuiosqueIdInOrderByDataHoraPedidoDesc(List<Long> ids, Pageable pageable);
   
   boolean existsByClienteIdAndStatusNotIn(Long clienteId, List<StatusPedido> status);
   
   boolean existsByEntregadorIdAndStatusNotIn(Long entregadorId, List<StatusPedido> status);
   
   Optional<Pedido> findByQuiosqueIdAndEntregadorIdAndStatusNotIn(Long quiosqueid, Long entregadorId, List<StatusPedido> status);
   
   Optional<Pedido> findByClienteAndId(Usuario cliente, UUID id);
   
   Optional<Pedido> findByIdAndEntregador(UUID id, Usuario entregador);
   
   Optional<Pedido> findByIdAndQuiosque(UUID id, Quiosque quiosque);
   
   @Query(value = """
		    SELECT
		        p.id_quiosque AS quiosqueId,

		        SUM(
			    CASE
			        WHEN p.data_hora_entrega                  >= :inicioHoje
			         AND p.data_hora_entrega                  < :fimHoje
			        THEN 1
			        ELSE 0
			    END
					) AS pedidosHoje,

		        SUM(
			    CASE
			        WHEN p.data_hora_entrega                     >= :inicioOntem
			         AND p.data_hora_entrega                     < :fimOntem
			        THEN 1
			        ELSE 0
			    END
					) AS pedidosOntem,

		        COALESCE(
		            SUM(
		                CASE
		                    WHEN p.data_hora_entrega             >= :inicioHoje
		                     AND p.data_hora_entrega             < :fimHoje
		                    THEN p.valor_total
		                    ELSE 0
		                END
		            ),
		            0
		        ) AS faturamentoHoje,

		        COALESCE(
		            SUM(
		                CASE
		                    WHEN p.data_hora_entrega           >= :inicioOntem
		                     AND p.data_hora_entrega           < :fimOntem
		                    THEN p.valor_total
		                    ELSE 0
		                END
		            ),
		            0
		        ) AS faturamentoOntem,

		        AVG(
		            CASE
		                WHEN p.data_hora_entrega           >= :inicioHoje
		                 AND p.data_hora_entrega           < :fimHoje
		                 AND p.data_hora_entrega IS NOT NULL
		                 AND p.data_hora_pedido IS NOT NULL
		                THEN TIMESTAMPDIFF(
		                    MINUTE,
		                    p.data_hora_pedido,
		                    p.data_hora_entrega
		                )
		            END
		        ) AS tempoMedioHoje,

		        AVG(
		            CASE
		                WHEN p.data_hora_entrega          >= :inicioOntem
		                 AND p.data_hora_entrega          < :fimOntem
		                 AND p.data_hora_entrega IS NOT NULL
		                 AND p.data_hora_pedido IS NOT NULL
		                THEN TIMESTAMPDIFF(
		                    MINUTE,
		                    p.data_hora_pedido,
		                    p.data_hora_entrega
		                )
		            END
		        ) AS tempoMedioOntem

		    FROM pedido p

		    WHERE p.id_quiosque IN (:quiosqueIds)
		      AND p.status = 'FINALIZADO'
		      AND p.data_hora_entrega      >= :inicioOntem
		      AND p.data_hora_entrega      < :fimHoje

		    GROUP BY p.id_quiosque
		    """, nativeQuery = true)
		List<QuiosqueMetricasProjection> buscarMetricasOverview(
		        @Param("quiosqueIds") List<Long> quiosqueIds,
		        @Param("inicioHoje") LocalDateTime inicioHoje,
		        @Param("fimHoje") LocalDateTime fimHoje,
		        @Param("inicioOntem") LocalDateTime inicioOntem,
		        @Param("fimOntem") LocalDateTime fimOntem
		);
   
   @Query(value = """
		    SELECT
		        p.id_quiosque AS quiosqueId,

		        SUM(
			    CASE
			        WHEN p.data_hora_entrega                  >= :inicioHoje
			         AND p.data_hora_entrega                  < :fimHoje
			        THEN 1
			        ELSE 0
			    END
					) AS pedidosHoje,

		        SUM(
			    CASE
			        WHEN p.data_hora_entrega                     >= :inicioOntem
			         AND p.data_hora_entrega                     < :fimOntem
			        THEN 1
			        ELSE 0
			    END
					) AS pedidosOntem,

		        COALESCE(
		            SUM(
		                CASE
		                    WHEN p.data_hora_entrega             >= :inicioHoje
		                     AND p.data_hora_entrega             < :fimHoje
		                    THEN p.valor_total
		                    ELSE 0
		                END
		            ),
		            0
		        ) AS faturamentoHoje,

		        COALESCE(
		            SUM(
		                CASE
		                    WHEN p.data_hora_entrega           >= :inicioOntem
		                     AND p.data_hora_entrega           < :fimOntem
		                    THEN p.valor_total
		                    ELSE 0
		                END
		            ),
		            0
		        ) AS faturamentoOntem,

		        AVG(
		            CASE
		                WHEN p.data_hora_entrega           >= :inicioHoje
		                 AND p.data_hora_entrega           < :fimHoje
		                 AND p.data_hora_entrega IS NOT NULL
		                 AND p.data_hora_pedido IS NOT NULL
		                THEN TIMESTAMPDIFF(
		                    MINUTE,
		                    p.data_hora_pedido,
		                    p.data_hora_entrega
		                )
		            END
		        ) AS tempoMedioHoje,

		        AVG(
		            CASE
		                WHEN p.data_hora_entrega          >= :inicioOntem
		                 AND p.data_hora_entrega          < :fimOntem
		                 AND p.data_hora_entrega IS NOT NULL
		                 AND p.data_hora_pedido IS NOT NULL
		                THEN TIMESTAMPDIFF(
		                    MINUTE,
		                    p.data_hora_pedido,
		                    p.data_hora_entrega
		                )
		            END
		        ) AS tempoMedioOntem

		    FROM pedido p

		    WHERE p.id_quiosque = (:quiosqueId)
		      AND p.status = 'FINALIZADO'
		      AND p.data_hora_entrega      >= :inicioOntem
		      AND p.data_hora_entrega      < :fimHoje

		    GROUP BY p.id_quiosque
		    """, nativeQuery = true)
		List<QuiosqueMetricasProjection> buscarMetricasOverview(
		        @Param("quiosqueId") Long quiosqueId,
		        @Param("inicioHoje") LocalDateTime inicioHoje,
		        @Param("fimHoje") LocalDateTime fimHoje,
		        @Param("inicioOntem") LocalDateTime inicioOntem,
		        @Param("fimOntem") LocalDateTime fimOntem
		);
   
}