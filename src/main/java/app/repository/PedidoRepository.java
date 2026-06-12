package app.repository;


//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusPedido;

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
	
   boolean existsByClienteIdAndStatusNotIn(Long clienteId, List<StatusPedido> status);
   
   boolean existsByEntregadorIdAndStatusNotIn(Long entregadorId, List<StatusPedido> status);
   
   Optional<Pedido> findByQuiosqueIdAndEntregadorIdAndStatusNotIn(Long quiosqueid, Long entregadorId, List<StatusPedido> status);
   
   Optional<Pedido> findByClienteAndId(Usuario cliente, UUID id);
   
   Optional<Pedido> findByIdAndEntregador(UUID id, Usuario entregador);
   
   Optional<Pedido> findByIdAndQuiosque(UUID id, Quiosque quiosque);
}
