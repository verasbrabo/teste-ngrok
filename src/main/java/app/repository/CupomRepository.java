package app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.entity.Cupom;

public interface CupomRepository extends JpaRepository<Cupom, Long>{

	// Mantem a consulta usada na criacao do pedido; a entidade ainda valida
	// status, exclusao e expiracao antes de conceder o desconto.
	Optional<Cupom> findByIdAndQuiosqueId(Long id, Long quiosque_id);

	Optional<Cupom> findByIdAndQuiosqueIdAndExcluidoFalse(Long id, Long quiosque_id);

	List<Cupom> findByQuiosqueIdAndAtivoAndDataExpiracaoAfterAndExcluidoFalse(
		    Long id_quiosque,
		    Boolean ativo,
		    LocalDateTime agora
		);

	/** Retorna também cupons inativos e expirados para a tela administrativa. */
	List<Cupom> findByQuiosqueIdAndExcluidoFalseOrderByIdDesc(Long id_quiosque);

	/** Desativa em uma unica operacao todos os cupons que atingiram a validade. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update Cupom c
			set c.ativo = false
			where c.ativo = true
			  and c.excluido = false
			  and c.dataExpiracao <= :agora
			""")
	int desativarExpirados(@Param("agora") LocalDateTime agora);
}
