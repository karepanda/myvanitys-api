package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;

public class ProductValidationException extends DomainException {

  public ProductValidationException(String message) {
    super(message);
  }

  public ProductValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
