package app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import app.auth.AuthRepository;
import app.enums.UserRole;
import jakarta.servlet.FilterChain;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityFilterTest {

	private TokenService tokenService;
	private AuthRepository authRepository;
	private FilterChain filterChain;
	private SecurityFilter securityFilter;

	@BeforeEach
	void configurar() {
		tokenService = mock(TokenService.class);
		authRepository = mock(AuthRepository.class);
		filterChain = mock(FilterChain.class);
		securityFilter = new SecurityFilter();
		securityFilter.tokenService = tokenService;
		securityFilter.userRepository = authRepository;
	}

	@Test
	void deveResponderUnauthorizedQuandoTokenForInvalido() throws Exception {
		MockHttpServletRequest request = requisicaoComToken("token-antigo");
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(tokenService.validateTokenPayload("token-antigo")).thenReturn(null);

		securityFilter.doFilterInternal(request, response, filterChain);

		assertEquals(401, response.getStatus());
		verifyNoInteractions(authRepository);
		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	void deveResponderUnauthorizedQuandoContaDoTokenNaoExistir() throws Exception {
		MockHttpServletRequest request = requisicaoComToken("token-sem-conta");
		MockHttpServletResponse response = new MockHttpServletResponse();
		TokenService.TokenPayload payload =
				new TokenService.TokenPayload("cliente@email.com", UserRole.CLIENTE);
		when(tokenService.validateTokenPayload("token-sem-conta")).thenReturn(payload);
		when(authRepository.findByLoginAndRole(payload.login(), payload.role()))
				.thenReturn(Optional.empty());

		securityFilter.doFilterInternal(request, response, filterChain);

		assertEquals(401, response.getStatus());
		verify(filterChain, never()).doFilter(request, response);
	}

	private MockHttpServletRequest requisicaoComToken(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + token);
		return request;
	}
}
