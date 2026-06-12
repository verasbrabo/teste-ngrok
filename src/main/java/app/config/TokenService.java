package app.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import app.entity.Usuario;

@Service
public class TokenService {
	
	@Value("${api.security.token.secret}")	
	private String secret;
	
	public String generateToken(Usuario user) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			String token = JWT.create()
					.withIssuer("Por-do-Sol")
					.withSubject(user.getEmail())
					.withClaim("role", user.getRole().name())
					.withClaim("trocarSenha", user.getUltimoLogin() == null)
					.withExpiresAt(genExpirationDate())
					.sign(algorithm);
			return token;
		} catch (JWTCreationException exception) {
			throw new RuntimeException("Error while generating token", exception);
		}
	}
	
	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.require(algorithm)
					.withIssuer("Por-do-Sol")
					.build()
					.verify(token)
					.getSubject();
		} catch (JWTVerificationException exception) {
			return "";
		}
	}
	
	
	private Instant genExpirationDate() {
		                           // mudei de 2 horas pra 2 anos
		// return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
		return LocalDateTime.now().plusYears(2).toInstant(ZoneOffset.of("-03:00"));	
	}
}
