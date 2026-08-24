package com.myvanitys.api.auth.infrastructure.security;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Holds the authenticated user id for the current HTTP request.
 *
 * <p>This is a request-scoped bean: the {@link JwtAuthenticationFilter} resolves and stores the
 * userId once per request, and controllers read it from here instead of re-parsing the
 * Authorization header. The request scope (backed by Spring's request attributes) ensures the
 * value is isolated per request and never leaks across the servlet container's thread pool.</p>
 */
@Component
@RequestScope
public class AuthenticatedUserContext {

  private UUID userId;

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }
}
