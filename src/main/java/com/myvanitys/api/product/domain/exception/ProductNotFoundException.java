package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public class ProductNotFoundException extends DomainException {

  public ProductNotFoundException(String message) {
    super(message);
  }

  public static ProductNotFoundException forUser(EntityId userId) {
    return new ProductNotFoundException("No products found for user: " + userId.getValue());
  }
}
