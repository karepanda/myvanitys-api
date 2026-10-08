package com.myvanitys.api.auth.domain.exception;

import com.myvanitys.api.common.DomainException;

public class UserRegistrationFailedException extends DomainException {

  public UserRegistrationFailedException(String message) {
    super(message);
  }

  public UserRegistrationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
