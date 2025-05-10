package com.myvanitys.api.auth.domain.exception;

import com.myvanitys.api.common.DomainException;

public class AuthenticationFailedException extends DomainException {

  public AuthenticationFailedException(String message) {
    super(message);
  }

  public AuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
