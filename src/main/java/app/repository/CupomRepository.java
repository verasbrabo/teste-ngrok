package app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Cupom;

public interface CupomRepository extends JpaRepository<Cupom, Long>{
	
	Optional<Cupom> findByIdAndQuiosqueId(Long id, Long quiosque_id);

	List<Cupom> findByQuiosqueIdAndAtivoAndDataExpiracaoAfter(
		    Long id_quiosque,
		    Boolean ativo,
		    LocalDateTime agora
		);
}
