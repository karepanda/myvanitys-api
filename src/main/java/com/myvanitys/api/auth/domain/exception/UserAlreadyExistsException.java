package com.myvanitys.api.auth.domain.exception;

import com.myvanitys.api.common.DomainException;

public class UserAlreadyExistsException extends DomainException {

  public UserAlreadyExistsException(String message) {
    super(message);
  }

  public UserAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
