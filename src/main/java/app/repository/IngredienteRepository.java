package app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Ingrediente;
import app.entity.Quiosque;

import java.util.Collection;
import java.util.List;


public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

	List<Ingrediente> findByNomeInIgnoreCase(Collection<String> nome);

	Optional<Ingrediente> findByNomeIgnoreCase(String nome);

	// Igualdade direta na coluna: como ingrediente.nome usa a collation
	// utf8mb4_0900_ai_ci (insensível a acento e caixa), este lookup casa
	// "limao" com o "limão" já existente — ao contrário do IgnoreCase, que
	// gera UPPER() e é sensível a acento. findFirst evita NonUnique caso a
	// base já tenha variações acentuadas duplicadas.
	Optional<Ingrediente> findFirstByNome(String nome);

}
