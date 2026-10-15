package app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.entity.Categoria;
import app.entity.Quiosque;
import app.enums.Categorias;
import java.util.List;
import app.entity.Item;





public interface CategoriaRepository extends JpaRepository<Categoria, Long>{

	boolean existsByQuiosqueIdAndNomeAndAtivoTrue(Long quiosqueId, Categorias nome);

	boolean existsByQuiosqueIdAndNomeAndAtivoTrueAndIdNot(
			Long quiosqueId, Categorias nome, Long categoriaId);
	
	Optional<Categoria> findByQuiosqueAndId(Quiosque quiosque, long id);
	
	List<Categoria> findByQuiosque(Quiosque quiosque);
	
	List<Categoria> findByQuiosqueOrderByOrdem(Quiosque quiosque);
	
	Optional<Categoria> findByItens(Item itens);

	@Query("""
			SELECT c.nome
			FROM Categoria c
			WHERE c.quiosque.id = :quiosqueId
			  AND c.ativo = true
			""")
	List<Categorias> buscarNomesAtivosPorQuiosqueId(
			@Param("quiosqueId") Long quiosqueId);
}
