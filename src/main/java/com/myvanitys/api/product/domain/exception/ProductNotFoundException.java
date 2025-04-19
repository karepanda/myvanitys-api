package com.myvanitys.api.product.domain.exception;

import com.myvanitys.api.common.DomainException;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public class ProductNotFoundException extends DomainException {

  // Public constructor that anyone can use
  public ProductNotFoundException(String message) {
    super(message);
  }

  // Public factory method to create an instance of ProductNotFoundException
  public static ProductNotFoundException forUser(EntityId userId) {
    return new ProductNotFoundException("No products found for user: " + userId.getValue());
  }
}
