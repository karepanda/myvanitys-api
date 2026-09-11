package com.myvanitys.api.auth.infrastructure.security;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvanitys.api.model.v1.ProblemDetail;
import com.myvanitys.api.product.infrastructure.adapter.primary.service.TokenService;
import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Centralized JWT authentication filter.
 *
 * <p>This is the single place where the Authorization header is read and the JWT is validated.
 * Validation is delegated to the existing {@link TokenService} (which wraps the
 * {@code JwtTokenGeneratorAdapter} that verifies the HMAC signature and expiry). On success the
 * resolved userId is stored in the request-scoped {@link AuthenticatedUserContext}; on failure the
 * request is short-circuited with a 401 and a {@code ProblemDetail} body consistent with
 * {@code GlobalExceptionHandler}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /**
   * Paths that are genuinely public (they either issue a token or are infrastructure probes) and
   * therefore do not require a pre-existing JWT. Everything else (including {@code /api/v1/products}
   * and {@code /api/v1/products/search}) requires authentication.
   */
  private static final Set<String> PUBLIC_PATHS = Set.of(
      "/api/v1/auth/google",
      "/api/v1/auth/register"
  );

  private static final String ACTUATOR_HEALTH_PREFIX = "/actuator/health";

  private static final URI UNAUTHORIZED_TYPE = URI.create("https://api.myvanitys.com/problems/unauthorized");

  private final TokenService tokenService;

  private final AuthenticatedUserContext authenticatedUserContext;

  private final ObjectMapper objectMapper;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // CORS preflight requests never carry credentials and must not be rejected.
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    String path = contextRelativePath(request);
    if (PUBLIC_PATHS.contains(path)) {
      return true;
    }
    return path.equals(ACTUATOR_HEALTH_PREFIX) || path.startsWith(ACTUATOR_HEALTH_PREFIX + "/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String authorization = request.getHeader("Authorization");
      UUID userId = tokenService.extractUserId(authorization);
      authenticatedUserContext.setUserId(userId);
      filterChain.doFilter(request, response);
    } catch (UnauthorizedException ex) {
      log.debug("Authentication rejected for {}: {}", request.getRequestURI(), ex.getMessage());
      writeUnauthorized(request, response);
    }
  }

  private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    ProblemDetail problem = new ProblemDetail()
        .type(UNAUTHORIZED_TYPE)
        .title("Authentication Required")
        .status(HttpServletResponse.SC_UNAUTHORIZED)
        .detail("Full authentication is required to access this resource")
        .instance(URI.create(request.getRequestURI()));

    objectMapper.writeValue(response.getWriter(), problem);
  }

  private String contextRelativePath(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    String uri = request.getRequestURI();
    if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
      return uri.substring(contextPath.length());
    }
    return uri;
  }
}
