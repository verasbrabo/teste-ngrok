package app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Categoria;
import app.entity.Item;
import app.entity.Quiosque;

public interface ItemRepository extends JpaRepository<Item, Long> {

	public List<Item> findByCategoriaQuiosque(Quiosque quiosque);
	
	public Optional<Item> findByIdAndCategoriaQuiosque(long id, Quiosque quiosque);
	
	public Optional<Item> findByIdAndCategoria(long id, Categoria categoria);
	
	Optional<Item> findByIdAndCategoriaIdAndCategoriaQuiosque(
		    Long itemId,
		    Long categoriaId,
		    Quiosque quiosque
		);
	
	Optional<Item> findByIdAndCategoriaIdAndCategoriaQuiosqueId(
		    Long itemId,
		    Long categoriaId,
		    Long quiosqueId
		);
	
	Optional<Item> findByIdAndCategoriaQuiosqueId(
		    Long itemId,
		    Long quiosqueId
		);
}
