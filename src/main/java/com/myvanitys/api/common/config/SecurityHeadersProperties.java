package com.myvanitys.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for the security-related HTTP response headers applied by
 * {@link SecurityHeadersFilter}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeadersProperties {

  /**
   * Emit the {@code Strict-Transport-Security} header on secure requests.
   */
  private boolean hsts = true;

  /**
   * HSTS max-age, in seconds.
   */
  private long hstsMaxAgeSeconds = 31536000;

  /**
   * Include the {@code includeSubDomains} directive in the HSTS header.
   */
  private boolean hstsIncludeSubDomains = true;

  /**
   * Include the {@code preload} directive in the HSTS header.
   */
  private boolean hstsPreload = false;
}
