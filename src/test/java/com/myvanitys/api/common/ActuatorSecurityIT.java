package com.myvanitys.api.common;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.myvanitys.api.auth.infrastructure.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorSecurityIT extends AbstractIntegrationTest {

  @TestConfiguration
  @Profile("test")
  static class TestConfig {

    @Bean
    @Primary
    TokenService tokenService() {
      TokenService tokenService = mock(TokenService.class);
      when(tokenService.extractUserId(anyString())).thenReturn(UUID.randomUUID());
      return tokenService;
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldExposeHealthProbeWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void shouldExposeLivenessProbeWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/health/liveness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void shouldExposeReadinessProbeWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/health/readiness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void shouldNotLeakHealthDetailsPublicly() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.components").doesNotExist());
  }

  @Test
  void shouldNotExposeDetailedDiagnosticsToAuthenticatedUser() throws Exception {
    mockMvc.perform(get("/actuator/env").header("Authorization", "Bearer test-token"))
        .andExpect(status().isNotFound());
  }
}
