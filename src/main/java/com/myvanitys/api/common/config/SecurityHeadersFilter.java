package com.myvanitys.api.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Applies consistent security response headers to every response, on both success and error paths.
 *
 * <p>The headers are set before the rest of the filter chain runs, so they are present even when a
 * downstream filter (such as {@code JwtAuthenticationFilter}) or the global exception handler
 * short-circuits the request.</p>
 *
 * <p>Transport security (HSTS) is emitted when the request is served directly over TLS or when
 * Railway, which terminates public TLS at its edge, reports the original HTTPS scheme through
 * {@code X-Forwarded-Proto: https}. That header intentionally influences only the HSTS decision.
 * No forwarded host, port, prefix, or RFC 7239 {@code Forwarded} value is used, so forwarded input
 * cannot create a redirect or otherwise select a response host. Railway must overwrite a
 * client-supplied {@code X-Forwarded-Proto}; the deployment verification procedure checks that
 * trust-boundary assumption.</p>
 *
 * <p>HTTP-to-HTTPS redirection is deliberately not implemented here. The managed public edge
 * (currently Cloudflare in front of Railway for the custom domain) already answers plain port-80
 * traffic with a redirect, while Railway sets {@code X-Forwarded-Proto: https} on requests it
 * forwards to the service. An application-level redirect is therefore unreachable for public
 * production traffic and would only introduce an open-redirect surface. See {@code
 * doc/operations/SEC-11-https-response-headers.md}.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class SecurityHeadersFilter extends OncePerRequestFilter {

  public static final String X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";

  public static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";

  public static final String REFERRER_POLICY_HEADER = "Referrer-Policy";

  public static final String STRICT_TRANSPORT_SECURITY_HEADER = "Strict-Transport-Security";

  private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

  private static final String HTTPS = "https";

  private final SecurityHeadersProperties properties;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    response.setHeader(X_CONTENT_TYPE_OPTIONS_HEADER, "nosniff");
    response.setHeader(X_FRAME_OPTIONS_HEADER, "DENY");
    response.setHeader(REFERRER_POLICY_HEADER, "no-referrer");

    if (isSecure(request) && properties.isHsts()) {
      response.setHeader(STRICT_TRANSPORT_SECURITY_HEADER, buildHstsHeader());
    }

    filterChain.doFilter(request, response);
  }

  private boolean isSecure(HttpServletRequest request) {
    if (request.isSecure()) {
      return true;
    }
    return HTTPS.equalsIgnoreCase(request.getHeader(FORWARDED_PROTO_HEADER));
  }

  private String buildHstsHeader() {
    StringBuilder value = new StringBuilder("max-age=").append(properties.getHstsMaxAgeSeconds());
    if (properties.isHstsIncludeSubDomains()) {
      value.append("; includeSubDomains");
    }
    if (properties.isHstsPreload()) {
      value.append("; preload");
    }
    return value.toString();
  }
}
