package app.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Funcionario;
import app.enums.UserRole;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

	boolean existsByCnpj(String cnpj);

	Optional<Funcionario> findByEmailIgnoreCaseAndRoleIn(
			String email, Collection<UserRole> roles);

	boolean existsByEmailIgnoreCaseAndRoleIn(
			String email, Collection<UserRole> roles);

	boolean existsByEmailIgnoreCaseAndRoleInAndIdNot(
			String email, Collection<UserRole> roles, Long id);
}
