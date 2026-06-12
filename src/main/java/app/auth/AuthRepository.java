package app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import app.entity.Usuario;
import java.util.Optional;
import java.util.UUID;

import app.entity.Quiosque;


public interface AuthRepository extends JpaRepository<Usuario, Long>{
	
	boolean existsByEmailAndRole(String email, String role);
	
	UserDetails findByEmail(String email);
	// UserDetails findByPublicId(UUID id);
	Optional<Usuario> findByQuiosqueAndId(Quiosque quiosque, Long id);
	
	Optional<Usuario> findByQuiosqueAndPublicId(Quiosque quiosque, UUID id);
	
	void deleteByPublicId(UUID publicId);
}
