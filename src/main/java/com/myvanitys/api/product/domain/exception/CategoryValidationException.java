package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;

public class CategoryValidationException extends DomainException {

  public CategoryValidationException(String message) {
    super(message);
  }

}
