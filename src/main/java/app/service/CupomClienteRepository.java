package app.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.entity.CupomCliente;
import app.entity.Cliente;


@Repository
public interface CupomClienteRepository extends JpaRepository<CupomCliente, Long> {
	
	boolean existsByCupomIdAndClienteId(Long cupomId, Long clienteId);

	/** Libera o cupom quando o pedido associado nao sera concluido. */
	long deleteByCupomIdAndClienteId(Long cupomId, Long clienteId);
}
