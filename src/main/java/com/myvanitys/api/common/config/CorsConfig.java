package com.myvanitys.api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "https://myvanitys.com",
            "https://www.myvanitys.com",
            "http://localhost:5173" // For local development
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();

    config.addAllowedOrigin("https://myvanitys.com");
    config.addAllowedOrigin("https://www.myvanitys.com");
    config.addAllowedOrigin("http://localhost:5173");
    config.addAllowedOrigin("https://www.myvanitys.com/callback");

    config.setAllowCredentials(true);

    config.addAllowedMethod("*");

    config.addAllowedHeader("*");

    config.addExposedHeader("Authorization");

    config.setMaxAge(3600L);

    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}