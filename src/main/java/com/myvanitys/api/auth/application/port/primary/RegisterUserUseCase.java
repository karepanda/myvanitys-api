package com.myvanitys.api.auth.application.port.primary;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.application.port.primary.result.UserRegistrationResult;
import reactor.core.publisher.Mono;

public interface RegisterUserUseCase {

  /**
   * Registers a new user using social provider authentication.
   *
   * @param command The registration command containing provider information and authorization code
   * @return The authentication response with tokens for the newly registered user
   */
  Mono<UserRegistrationResult> execute(RegisterUserCommand command, UUID requestId, UUID flowId);
}


