package com.myvanitys.api.auth.infrastructure.adapter.primary;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.RegisterUserUseCase;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.application.port.primary.result.UserRegistrationResult;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.AuthenticationMapper;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.CreateUserMapper;
import com.myvanitys.api.model.v1.AuthResponse;
import com.myvanitys.api.model.v1.CreateUserRequest;
import com.myvanitys.api.model.v1.GoogleAuthRequest;
import com.myvanitys.api.model.v1.UserCreatedResponse;
import com.myvanitys.api.rest.v1.AuthenticationApiDelegate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AuthController implements AuthenticationApiDelegate {

  private final GoogleAuthenticationUseCase googleAuthenticationUseCase;

  private final AuthenticationMapper authenticationMapper;

  private final CreateUserMapper createUserMapper;

  private final RegisterUserUseCase registerUserUseCase;

  @Override
  public ResponseEntity<AuthResponse> authenticateWithGoogle(
      UUID xRequestID,
      UUID xFlowID,
      @Valid GoogleAuthRequest googleAuthRequest) {

    // Map request to domain command
    GoogleAuthCommand command = authenticationMapper.toCommand(googleAuthRequest);

    // Calling the use case
    UserSession session = googleAuthenticationUseCase.authenticateWithGoogle(command, xRequestID, xFlowID).block();

    // Map user session to API response
    AuthResponse response = authenticationMapper.toResponse(session);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<UserCreatedResponse> createUser(
      UUID xRequestID,
      UUID xFlowID,
      @Valid CreateUserRequest createUserRequest) {

    RegisterUserCommand command = createUserMapper.toCommand(createUserRequest);

    UserRegistrationResult result = registerUserUseCase.execute(command, xRequestID, xFlowID).block();
    if (result == null) {
      log.error("User registration result was null for request ID: {}, flow ID: {}", xRequestID, xFlowID);
      return ResponseEntity.internalServerError().build();
    }

    UserCreatedResponse response = createUserMapper.toResponse(result.session().user());
    return ResponseEntity.ok(response);

  }

}
