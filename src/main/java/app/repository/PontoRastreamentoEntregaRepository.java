package app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Pedido;
import app.entity.PontoRastreamentoEntrega;

public interface PontoRastreamentoEntregaRepository
		extends JpaRepository<PontoRastreamentoEntrega, Long> {

	Optional<PontoRastreamentoEntrega> findFirstByPedidoOrderByCapturadoEmDesc(Pedido pedido);

	List<PontoRastreamentoEntrega> findTop500ByPedidoOrderByCapturadoEmDesc(Pedido pedido);
}
