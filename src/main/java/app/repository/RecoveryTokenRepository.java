package app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.RecoveryToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import app.entity.Usuario;

public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long>{
	
	Optional<RecoveryToken> findByUsuarioAndTokenAndExpiracaoBefore(Usuario usuario, String token, LocalDateTime expiracao);
	
	Optional<RecoveryToken> findByTokenAndExpiracaoAfter(String token, LocalDateTime agora);
	
	Boolean existsByUsuarioLoginAndEnviadoAfterAndExpiracaoAfter(String login, LocalDateTime ultimosMinutos, LocalDateTime agora);

	
	void deleteByExpiracaoBefore(LocalDateTime agora);
}
