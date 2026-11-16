package com.myvanitys.api.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogSanitizerTest {

  @Test
  void maskId_nullOrBlank_returnsMask() {
    assertEquals("***", LogSanitizer.maskId());
    assertEquals("***", LogSanitizer.maskId());
    assertEquals("***", LogSanitizer.maskId());
  }

  @Test
  void maskId_shortValue_returnsMask() {
    assertEquals("***", LogSanitizer.maskId());
  }

  @Test
  void maskId_longValue_returnsOpaqueMask() {
    assertEquals("***", LogSanitizer.maskId());
    assertEquals("***", LogSanitizer.maskId());
  }

  @Test
  void maskEmail_masksLocalPartAndKeepsDomain() {
    assertEquals("***@example.com", LogSanitizer.maskEmail("user@example.com"));
  }

  @Test
  void maskEmail_nullOrInvalid_returnsMask() {
    assertEquals("***", LogSanitizer.maskEmail(null));
    assertEquals("***", LogSanitizer.maskEmail("not-an-email"));
  }
}
