package app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Categoria;
import app.entity.Quiosque;
import java.util.List;
import app.entity.Item;





public interface CategoriaRepository extends JpaRepository<Categoria, Long>{
	
	Optional<Categoria> findByQuiosqueAndId(Quiosque quiosque, long id);
	
	List<Categoria> findByQuiosque(Quiosque quiosque);
	
	List<Categoria> findByQuiosqueOrderByOrdem(Quiosque quiosque);
	
	Optional<Categoria> findByItens(Item itens);
}
