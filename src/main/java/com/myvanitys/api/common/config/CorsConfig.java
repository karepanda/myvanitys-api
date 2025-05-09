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
        .maxAge(3600); // 1 hour cache for CORS configuration
  }

  // This bean provides more detailed CORS configuration if needed
  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();

    // Allowed origins
    config.addAllowedOrigin("https://myvanitys.com");
    config.addAllowedOrigin("https://www.myvanitys.com");
    config.addAllowedOrigin("http://localhost:5173");
    config.addAllowedOrigin("https://www.myvanitys.com/callback"); // For local development

    // Enable credentials (cookies, authorization headers)
    config.setAllowCredentials(true);

    // Allowed HTTP methods
    config.addAllowedMethod("*");

    // Allowed headers
    config.addAllowedHeader("*");

    // Expose specific headers to the client
    config.addExposedHeader("Authorization");

    // CORS preflight cache
    config.setMaxAge(3600L);

    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}