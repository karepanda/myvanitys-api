package com.myvanitys.api.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates a correlation identifier on every response so clients and operators can trace an
 * error back to the originating request.
 *
 * <p>Runs with highest precedence so the header is present even when a downstream filter (such as
 * the JWT authentication filter) short-circuits the request with its own error response.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Request-ID";

  public static final String FLOW_ID_HEADER = "X-Flow-ID";

  public static final String CORRELATION_ID_ATTRIBUTE =
      CorrelationIdFilter.class.getName() + ".correlationId";

  public static final String CORRELATION_ID_MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = resolveCorrelationId(request);
    String previousCorrelationId = MDC.get(CORRELATION_ID_MDC_KEY);

    request.setAttribute(CORRELATION_ID_ATTRIBUTE, correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);
    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      if (previousCorrelationId == null) {
        MDC.remove(CORRELATION_ID_MDC_KEY);
      } else {
        MDC.put(CORRELATION_ID_MDC_KEY, previousCorrelationId);
      }
    }
  }

  private String resolveCorrelationId(HttpServletRequest request) {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (!StringUtils.hasText(correlationId)) {
      correlationId = request.getHeader(FLOW_ID_HEADER);
    }
    if (!StringUtils.hasText(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }
    return correlationId;
  }
}
