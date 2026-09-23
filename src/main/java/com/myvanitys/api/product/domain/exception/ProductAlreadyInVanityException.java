package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;

public class ProductAlreadyInVanityException extends DomainException {

  public ProductAlreadyInVanityException(String message) {
    super(message);
  }

}
