package app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import app.entity.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import app.entity.Funcionario;
import app.entity.Quiosque;


public interface AuthRepository extends JpaRepository<Usuario, Long>{
	
	boolean existsByLoginAndRole(String login, String role);
	
	UserDetails findByLogin(String login);
	// UserDetails findByPublicId(UUID id);
	Optional<Funcionario> findByQuiosqueAndId(Quiosque quiosque, Long id);
	
	Optional<Funcionario> findByQuiosqueAndPublicId(Quiosque quiosque, UUID id);

	List<Funcionario> findByQuiosqueIdIn(List<Long> ids);
	
	List<Funcionario> findByQuiosqueId(Long id);
	
	void deleteByPublicId(UUID publicId);
	
	@Query  (""" 
			SELECT u.login
			FROM Usuario u
			WHERE u.login LIKE :base%
		    """)
	List<String> findLoginByBase(@Param("base") String base);
}
