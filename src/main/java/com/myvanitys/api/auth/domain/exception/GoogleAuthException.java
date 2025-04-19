package com.myvanitys.api.auth.domain.exception;

import com.myvanitys.api.common.DomainException;

public class GoogleAuthException extends DomainException {

  public GoogleAuthException(String message) {
    super(message);
  }

  public GoogleAuthException(String message, Throwable cause) {
    super(message, cause);
  }
}