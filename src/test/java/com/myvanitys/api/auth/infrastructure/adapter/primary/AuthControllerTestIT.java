package com.myvanitys.api.auth.infrastructure.adapter.primary;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.RegisterUserUseCase;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.application.port.primary.result.UserRegistrationResult;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.AuthenticationMapper;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.CreateUserMapper;
import com.myvanitys.api.common.AbstractIntegrationTest;
import com.myvanitys.api.model.v1.CreateUserRequest;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTestIT extends AbstractIntegrationTest {

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public GoogleAuthenticationUseCase googleAuthenticationUseCase() {
      return mock(GoogleAuthenticationUseCase.class);
    }

    @Bean
    @Primary
    public AuthenticationMapper authenticationMapper() {
      return mock(AuthenticationMapper.class);
    }

    @Bean
    @Primary
    public RegisterUserUseCase registerUserUseCase() {
      return mock(RegisterUserUseCase.class);
    }

    @Bean
    @Primary
    public CreateUserMapper createUserMapper() {
      return mock(CreateUserMapper.class);
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private GoogleAuthenticationUseCase googleAuthenticationUseCase;

  @Autowired
  private AuthenticationMapper authenticationMapper;

  @Autowired
  private CreateUserMapper createUserMapper;

  @Test
  void shouldAuthenticateWithGoogle() throws Exception {
    // Given
    String authCode = "4/0AbCd_ExAmPlE-CoD3";
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String token = "jwt-token-12345";
    String email = "user@example.com";
    String name = "Test User";

    // Creation of objects required for the test
    EntityId entityId = new EntityId(userId);
    User user = new User(entityId, "google-user-123", email, name);
    UserSession session = new UserSession(token, user);

    GoogleAuthCommand command = GoogleAuthCommand.of(authCode);
    when(authenticationMapper.toCommand(any())).thenReturn(command);

    // Prepare expected response
    com.myvanitys.api.model.v1.AuthResponse authResponse = new com.myvanitys.api.model.v1.AuthResponse()
        .token(token)
        .userId(userId)
        .email(email)
        .name(name);
    when(authenticationMapper.toResponse(any(UserSession.class))).thenReturn(authResponse);

    // Simulating use case behavior
    when(googleAuthenticationUseCase.authenticateWithGoogle(command, requestId, flowId))
        .thenReturn(Mono.just(session)); // Debemos devolver un Mono de UserSession

    // When
    mockMvc.perform(post("/api/v1/auth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .content("{\"code\":\"" + authCode + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(token))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.name").value(name));
  }

  @Test
  @Disabled("Pending to implement exception handling")
  void shouldReturnBadRequestWhenCodeIsMissing() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();

    // Then
    mockMvc.perform(post("/api/v1/auth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenHeadersAreMissing() throws Exception {
    // Given
    String authCode = "4/0AbCd_ExAmPlE-CoD3";

    // Then
    mockMvc.perform(post("/api/v1/auth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"" + authCode + "\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnInternalServerErrorWhenAuthenticationFails() throws Exception {
    // Given
    String authCode = "invalid-auth-code";
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();

    // Configuring mapper behavior
    GoogleAuthCommand command = GoogleAuthCommand.of(authCode);
    when(authenticationMapper.toCommand(any())).thenReturn(command);

    // When
    when(googleAuthenticationUseCase.authenticateWithGoogle(any(GoogleAuthCommand.class), any(UUID.class), any(UUID.class)))
        .thenThrow(new RuntimeException("Authentication failed"));

    // Then
    mockMvc.perform(post("/api/v1/auth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .content("{\"code\":\"" + authCode + "\"}"))
        .andExpect(status().isInternalServerError());
  }

    @Test
    void shouldCreateUser() throws Exception {
      // Given
      String authCode = "4/0AbCd_ExAmPlE-CoD3";
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      String token = "jwt-token-12345";
      String email = "user@example.com";
      String name = "Test User";
      String provider = "google";
      //Create request
        CreateUserRequest createUserRequest = new CreateUserRequest()
                .authProvider(CreateUserRequest.AuthProviderEnum.GOOGLE)
                .authCode(authCode);

      // Creation of objects required for the test
      EntityId entityId = new EntityId(userId);
      User user = new User(entityId, "google-user-123", email, name);
      UserSession session = new UserSession(token, user);
      UserRegistrationResult registrationResult = new UserRegistrationResult(session);

      //Mock configuration
      RegisterUserCommand command = RegisterUserCommand.of(provider,authCode);
      when(createUserMapper.toCommand(any(CreateUserRequest.class))).thenReturn(command);

      when(registerUserUseCase.execute(any(RegisterUserCommand.class), any(UUID.class), any(UUID.class)))
              .thenReturn(Mono.error(new RuntimeException("Failed to create user"))); // Debemos devolver un Mono de UserSession

      //When/Then
      mockMvc.perform(post("/api/v1/auth/register")
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("X-Request-ID", requestId.toString())
                      .header("X-Flow-ID", flowId.toString())
                      .content("{\"authProvider\":\"GOOGLE\",\"authCode\":\"" + authCode + "\"}"))
              .andExpect(status().isInternalServerError());

    }
}