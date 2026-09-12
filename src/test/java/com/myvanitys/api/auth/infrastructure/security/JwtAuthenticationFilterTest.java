package com.myvanitys.api.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvanitys.api.auth.infrastructure.security.TokenService;
import com.myvanitys.api.common.UnauthorizedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

  private static final UUID USER_ID = UUID.fromString("01965972-7033-7950-9cb1-56fe1251e72e");

  private TokenService tokenService;
  private AuthenticatedUserContext context;
  private ObjectMapper objectMapper;
  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    tokenService = mock(TokenService.class);
    context = new AuthenticatedUserContext();
    objectMapper = new ObjectMapper();
    filter = new JwtAuthenticationFilter(tokenService, context, objectMapper);
  }

  private MockHttpServletRequest request(String method, String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod(method);
    request.setRequestURI(uri);
    return request;
  }

  @Test
  void shouldSkipGoogleAuthEndpoint() throws Exception {
    MockHttpServletRequest request = request("POST", "/api/v1/auth/google");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    verify(tokenService, never()).extractUserId(anyString());
    assertNotNull(chain.getRequest());
  }

  @Test
  void shouldSkipRegisterEndpoint() throws Exception {
    MockHttpServletRequest request = request("POST", "/api/v1/auth/register");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    verify(tokenService, never()).extractUserId(anyString());
  }

  @Test
  void shouldSkipActuatorHealthEndpoint() throws Exception {
    MockHttpServletRequest request = request("GET", "/actuator/health");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    verify(tokenService, never()).extractUserId(anyString());
  }

  @Test
  void shouldSkipCorsPreflightOptionsRequest() throws Exception {
    MockHttpServletRequest request = request("OPTIONS", "/api/v1/products");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    verify(tokenService, never()).extractUserId(anyString());
  }

  @Test
  void shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/v1/products");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    when(tokenService.extractUserId(null)).thenThrow(new UnauthorizedException("No bearer token found"));

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertNull(chain.getRequest());

    JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
    assertEquals(401, body.get("status").asInt());
    assertEquals("Authentication Required", body.get("title").asText());
  }

  @Test
  void shouldReturn401WhenTokenIsInvalid() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/v1/products/search");
    request.addHeader("Authorization", "Bearer invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    when(tokenService.extractUserId("Bearer invalid-token"))
        .thenThrow(new UnauthorizedException("Unauthorized: Invalid token"));

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertNull(chain.getRequest());
    assertNull(context.getUserId());
  }

  @Test
  void shouldStoreUserIdAndContinueWhenTokenIsValid() throws Exception {
    MockHttpServletRequest request = request("GET", "/api/v1/products");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    when(tokenService.extractUserId("Bearer valid-token")).thenReturn(USER_ID);

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
    assertEquals(USER_ID, context.getUserId());
  }
}
