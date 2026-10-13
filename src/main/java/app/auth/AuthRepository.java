package app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.entity.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

import app.entity.Funcionario;
import app.entity.Quiosque;
import app.enums.UserRole;


public interface AuthRepository extends JpaRepository<Usuario, Long>{
	
	boolean existsByLoginAndRole(String login, UserRole role);

	boolean existsByLoginAndRoleIn(String login, Collection<UserRole> roles);

	boolean existsByTelefoneAndRole(String telefone, UserRole role);

	boolean existsByTelefoneAndRoleIn(String telefone, Collection<UserRole> roles);

	boolean existsByLoginAndRoleAndIdNot(String login, UserRole role, Long id);

	boolean existsByLoginAndRoleInAndIdNot(
			String login, Collection<UserRole> roles, Long id);

	boolean existsByTelefoneAndRoleAndIdNot(String telefone, UserRole role, Long id);

	boolean existsByTelefoneAndRoleInAndIdNot(
			String telefone, Collection<UserRole> roles, Long id);

	Optional<Usuario> findByLoginAndRole(String login, UserRole role);

	Optional<Usuario> findByLoginAndRoleIn(String login, Collection<UserRole> roles);
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
