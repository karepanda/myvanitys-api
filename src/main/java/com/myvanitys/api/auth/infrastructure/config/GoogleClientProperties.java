package com.myvanitys.api.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.oauth2")
public class GoogleClientProperties {

  private String clientId;

  private String clientSecret;

  private String redirectUri;
}