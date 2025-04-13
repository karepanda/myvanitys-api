// infrastructure/config/SecurityConfig.java

package com.myvanitys.api.auth.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

  @Bean
  public Key jwtSigningKey(JwtProperties jwtProperties) {
    return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }
}