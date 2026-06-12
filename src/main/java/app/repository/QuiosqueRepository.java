package app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import app.entity.Quiosque;
import app.entity.Usuario;

public interface QuiosqueRepository extends JpaRepository<Quiosque, Long> {
	// Talvez adicionar um Where status = ativo
	@Query(value = "SELECT * "			
			+ " FROM quiosque"
			+ " WHERE latitude BETWEEN :minLat AND :maxLat"
			+ "  AND longitude BETWEEN :minLon AND :maxLon", nativeQuery = true)
	List<Quiosque> findByDistancia(double latUsuario,double lonUsuario, double raioM, double minLat, double maxLat, double minLon, double maxLon);
	
	@Query(value = "SELECT * "			 
	        + " FROM quiosque"
	        + " WHERE latitude BETWEEN :minLat AND :maxLat"
	        + "   AND longitude BETWEEN :minLon AND :maxLon"
	        + "   AND status = 'Ativa'", nativeQuery = true)
	List<Quiosque> findByDistanciaAtivo(double latUsuario,double lonUsuario, double raioM, double minLat, double maxLat, double minLon, double maxLon);
	
	Optional<Quiosque> findByFuncionarios(Usuario administrador);
	
	Optional<Quiosque> findByFuncionariosContaining(Usuario administrador);
	
	Optional<Quiosque> findByProprietario(Usuario proprietario);
}
