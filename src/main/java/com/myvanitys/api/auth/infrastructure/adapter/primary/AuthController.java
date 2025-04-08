package com.myvanitys.api.auth.infrastructure.adapter.primary;

import java.util.UUID;

import com.myvanitys.api.model.v1.AuthResponse;
import com.myvanitys.api.model.v1.GoogleAuthRequest;
import com.myvanitys.api.rest.v1.AuthenticationApiDelegate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthController implements AuthenticationApiDelegate {

  @Override
  public ResponseEntity<AuthResponse> authenticateWithGoogle(
      UUID xRequestID,
      UUID xFlowID,
      GoogleAuthRequest googleAuthRequest) throws Exception {

    // Invocar el caso de uso con el código de autorización de Google
    // AuthenticationResult result = googleAuthenticationUseCase.authenticateWithGoogle(googleAuthRequest.getCode());

    // Mapear los resultados al modelo de respuesta generado por OpenAPI
    //    User user = result.user();
    //    AuthResponse response = new AuthResponse()
    //        .token(result.token())
    //        .userId(user.getId().getValue())
    //        .email(user.getEmail())
    //        .name(user.getName());

    return ResponseEntity.ok(new AuthResponse());
  }
}
