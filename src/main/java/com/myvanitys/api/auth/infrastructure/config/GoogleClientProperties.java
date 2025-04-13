// infrastructure/config/GoogleClientProperties.java

package com.myvanitys.api.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.client")
public class GoogleClientProperties {

  private String id;

  private String secret;

  private String redirectUri = "http://localhost:5173/callback";
}