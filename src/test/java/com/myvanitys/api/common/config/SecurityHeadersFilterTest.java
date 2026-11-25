package com.myvanitys.api.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeadersFilterTest {

  private SecurityHeadersFilter filter(SecurityHeadersProperties properties) {
    return new SecurityHeadersFilter(properties);
  }

  private SecurityHeadersProperties properties() {
    return new SecurityHeadersProperties();
  }

  private MockHttpServletRequest request() {
    return new MockHttpServletRequest();
  }

  @Test
  void setsStaticSecurityHeadersOnSuccessfulResponse() throws Exception {
    MockHttpServletRequest request = request();
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean chainInvoked = new AtomicBoolean(false);

    filter(properties()).doFilter(request, response,
        (filteredRequest, filteredResponse) -> chainInvoked.set(true));

    assertEquals("nosniff", response.getHeader(SecurityHeadersFilter.X_CONTENT_TYPE_OPTIONS_HEADER));
    assertEquals("DENY", response.getHeader(SecurityHeadersFilter.X_FRAME_OPTIONS_HEADER));
    assertEquals("no-referrer", response.getHeader(SecurityHeadersFilter.REFERRER_POLICY_HEADER));
    assertNull(response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
    assertEquals(true, chainInvoked.get());
  }

  @Test
  void setsHstsHeaderWhenRequestIsDirectlySecure() throws Exception {
    MockHttpServletRequest request = request();
    request.setSecure(true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties()).doFilter(request, response, new MockFilterChain());

    assertEquals("max-age=31536000; includeSubDomains",
        response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
  }

  @Test
  void setsHstsHeaderWhenTrustedProxyReportsHttps() throws Exception {
    MockHttpServletRequest request = request();
    request.addHeader("X-Forwarded-Proto", "https");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties()).doFilter(request, response, new MockFilterChain());

    assertEquals("max-age=31536000; includeSubDomains",
        response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
  }

  @Test
  void omitsHstsHeaderWhenTrustedProxyReportsHttp() throws Exception {
    MockHttpServletRequest request = request();
    request.addHeader("X-Forwarded-Proto", "http");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties()).doFilter(request, response, new MockFilterChain());

    assertNull(response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
  }

  @Test
  void ignoresRfc7239ForwardedHeaderForSecurityDecision() throws Exception {
    MockHttpServletRequest request = request();
    request.addHeader("Forwarded", "for=198.51.100.1;proto=https;host=evil.example");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties()).doFilter(request, response, new MockFilterChain());

    assertNull(response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
  }

  @Test
  void omitsHstsHeaderWhenHstsDisabled() throws Exception {
    SecurityHeadersProperties properties = properties();
    properties.setHsts(false);
    MockHttpServletRequest request = request();
    request.setSecure(true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties).doFilter(request, response, new MockFilterChain());

    assertNull(response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
  }

  @Test
  void includesPreloadDirectiveWhenEnabled() throws Exception {
    SecurityHeadersProperties properties = properties();
    properties.setHstsPreload(true);
    MockHttpServletRequest request = request();
    request.setSecure(true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties).doFilter(request, response, new MockFilterChain());

    assertEquals("max-age=31536000; includeSubDomains; preload",
        response.getHeader(SecurityHeadersFilter.STRICT_TRANSPORT_SECURITY_HEADER));
  }

  @Test
  void setsHeadersWhenDownstreamChainWritesErrorResponse() throws Exception {
    MockHttpServletRequest request = request();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(properties()).doFilter(request, response, (filteredRequest, filteredResponse) -> {
      HttpServletResponse servletResponse = (HttpServletResponse) filteredResponse;
      servletResponse.setStatus(500);
      servletResponse.getWriter().write("boom");
    });

    assertEquals(500, response.getStatus());
    assertEquals("nosniff", response.getHeader(SecurityHeadersFilter.X_CONTENT_TYPE_OPTIONS_HEADER));
    assertEquals("DENY", response.getHeader(SecurityHeadersFilter.X_FRAME_OPTIONS_HEADER));
    assertEquals("no-referrer", response.getHeader(SecurityHeadersFilter.REFERRER_POLICY_HEADER));
  }
}
