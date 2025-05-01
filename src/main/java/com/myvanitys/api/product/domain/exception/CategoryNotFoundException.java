package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public class CategoryNotFoundException extends DomainException {

  public CategoryNotFoundException(String message) {
    super(message);
  }

  public static CategoryNotFoundException forUser(EntityId userId) {
    return new CategoryNotFoundException("No category found: " + userId.getValue());
  }
}
