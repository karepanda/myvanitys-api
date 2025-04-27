package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;

public class ReviewValidationException extends DomainException {

  public ReviewValidationException(String message) {
    super(message);
  }

  public ReviewValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}
