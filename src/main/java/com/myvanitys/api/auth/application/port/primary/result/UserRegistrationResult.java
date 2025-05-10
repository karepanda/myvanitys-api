package com.myvanitys.api.auth.application.port.primary.result;

import com.myvanitys.api.auth.domain.model.UserSession;

public record UserRegistrationResult(UserSession session) {

  public UserRegistrationResult {
    if (session == null) {
      throw new IllegalArgumentException("Session cannot be null");
    }
  }

}