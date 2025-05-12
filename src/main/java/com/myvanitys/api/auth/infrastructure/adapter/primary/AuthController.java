package com.myvanitys.api.auth.infrastructure.adapter.primary;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.AuthenticationMapper;
import com.myvanitys.api.model.v1.AuthResponse;
import com.myvanitys.api.model.v1.CreateUserRequest;
import com.myvanitys.api.model.v1.GoogleAuthRequest;
import com.myvanitys.api.model.v1.UserCreatedResponse;
import com.myvanitys.api.rest.v1.AuthenticationApiDelegate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthController implements AuthenticationApiDelegate {

  private final GoogleAuthenticationUseCase googleAuthenticationUseCase;

  private final AuthenticationMapper authenticationMapper;

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

    // Aquí tu lógica de registro usando tu caso de uso
    // Por ejemplo:
    // var command = authenticationMapper.toCreateUserCommand(createUserRequest);
    // var result = createUserUseCase.execute(command).block();
    // return ResponseEntity.ok(authenticationMapper.toCreateUserResponse(result));

    return ResponseEntity.status(HttpStatus.CREATED).build(); // temporal
  }

}
