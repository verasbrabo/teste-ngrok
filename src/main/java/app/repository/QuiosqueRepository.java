package app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.entity.Funcionario;
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
	
	   @Query("""
			    SELECT q.id
			    FROM Quiosque q
			    WHERE q.proprietario = :usuario
			""")
			List<Long> findIdsByProprietario(
			    @Param("usuario") Funcionario usuario
			);
	
	Optional<Quiosque> findByFuncionarios(Funcionario administrador);
	
	Optional<Quiosque> findByFuncionariosContaining(Funcionario administrador);
	
	List<Quiosque> findByProprietario(Funcionario proprietario);
	
	boolean existsByEmailOrCnpj(String email, String cnpj);
	
	@Query("""
		    SELECT
		        p.nota AS nota,
		        p.comentario AS comentario
		    FROM Pedido p
		    WHERE p.quiosque.id = :quiosqueId
		      AND p.status = app.enums.StatusPedido.AVALIADO
		      AND p.nota IS NOT NULL
		      AND p.comentario IS NOT NULL
		      AND TRIM(p.comentario) <> ''
		    ORDER BY p.dataHoraEntrega DESC
		""")
	    List<AvaliacaoProjection> buscarAvaliacoesDoRestaurante(
	        @Param("quiosqueId") Long quiosqueid
	    );
	
}
