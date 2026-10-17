package app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.RecoveryToken;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import app.entity.Usuario;

public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long>{
	
	Optional<RecoveryToken> findByUsuarioAndTokenAndExpiracaoBefore(Usuario usuario, String token, LocalDateTime expiracao);
	
	Optional<RecoveryToken> findByTokenAndUsuarioRoleInAndExpiracaoAfter(
			String token, Collection<app.enums.UserRole> roles, LocalDateTime agora);

	Boolean existsByUsuarioAndEnviadoAfterAndExpiracaoAfter(
			Usuario usuario, LocalDateTime ultimosMinutos, LocalDateTime agora);

	
	void deleteByExpiracaoBefore(LocalDateTime agora);
}
