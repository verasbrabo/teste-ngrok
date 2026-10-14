package app.config;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.auth.AuthRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter{
	
	@Autowired
	TokenService tokenService;
	@Autowired
	AuthRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var token = this.recoverToken(request);
		
		if(token != null) {
			var payload = tokenService.validateTokenPayload(token);
			if(payload == null) {
				responderTokenInvalido(response);
				return;
			}
			
			var user = userRepository.findByLoginAndRole(payload.login(), payload.role());
			
			if(user.isEmpty()) {
				responderTokenInvalido(response);
				return;
			}

			var authentication = new UsernamePasswordAuthenticationToken(
					user.get(), null, user.get().getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
		
	}
	
	public String recoverToken(HttpServletRequest request) {
		var authHeader = request.getHeader("Authorization");
		if(authHeader == null || !authHeader.startsWith("Bearer ")) return null;
		var token = authHeader.substring(7).trim();
		return token.isEmpty() ? null : token;
	}

	/** Token rejeitado e uma falha de autenticacao, nunca um erro do pedido. */
	private void responderTokenInvalido(HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalido ou expirado");
	}

	
}
