package app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Acompanhamento;
import app.entity.Item;
import app.entity.Quiosque;

public interface AcompanhamentoRepository extends JpaRepository<Acompanhamento, Long> {

	List<Acompanhamento> findByQuiosque(Quiosque quiosque);
	
	Optional<Acompanhamento> findByQuiosqueAndId(Quiosque quiosque, Long id);
	
	List<Acompanhamento> findByIdInAndQuiosque(Iterable<Long> ids, Quiosque quiosque);
	List<Acompanhamento> findByIdInAndItemsContainsAndQuiosque(Iterable<Long> ids, Item item, Quiosque quiosque);
}
