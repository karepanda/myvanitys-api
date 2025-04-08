package com.myvanitys.api.auth.infrastructure.adapter.primary;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.model.v1.AuthResponse;
import com.myvanitys.api.model.v1.GoogleAuthRequest;
import com.myvanitys.api.rest.v1.AuthenticationApiDelegate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthController implements AuthenticationApiDelegate {

  private final GoogleAuthenticationUseCase googleAuthenticationUseCase;

  //private final AuthenticationMapper authenticationMapper;

  @Override
  public ResponseEntity<AuthResponse> authenticateWithGoogle(
      UUID xRequestID,
      UUID xFlowID,
      GoogleAuthRequest googleAuthRequest) throws Exception {

    // Map request to domain command
    //GoogleAuthCommand command = authenticationMapper.toCommand(googleAuthRequest);

    // Calling the use case
    //UserSession session = googleAuthenticationUseCase.authenticateWithGoogle(command, xRequestID, xFlowID);

    // Map user session to API response
    //AuthResponse response = authenticationMapper.toResponse(session);

    return ResponseEntity.ok(new AuthResponse());
  }
}
