package com.myvanitys.api.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void echoesExistingCorrelationId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "request-123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<Object> requestCorrelationId = new AtomicReference<>();
    AtomicReference<String> mdcCorrelationId = new AtomicReference<>();

    filter.doFilter(request, response, (filteredRequest, filteredResponse) -> {
      assertSame(response, filteredResponse);
      requestCorrelationId.set(filteredRequest.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE));
      mdcCorrelationId.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    });

    assertEquals("request-123", response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    assertEquals("request-123", requestCorrelationId.get());
    assertEquals("request-123", mdcCorrelationId.get());
    assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
  }

  @Test
  void fallsBackToFlowIdWhenRequestIdAbsent() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.FLOW_ID_HEADER, "flow-456");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals("flow-456", response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
  }

  @Test
  void generatesCorrelationIdWhenNoneProvided() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    String correlationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
    assertNotNull(correlationId);
    assertEquals(36, correlationId.length());
    assertEquals(correlationId, request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE));
    assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
  }

  @Test
  void restoresPreviousMdcValueAfterRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "request-123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "outer-request");
    try {
      filter.doFilter(request, response, chain);
      assertEquals("outer-request", MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    } finally {
      MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
    }
  }
}
