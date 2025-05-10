package com.myvanitys.api.auth.domain.exception;

import com.myvanitys.api.common.DomainException;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public class UserNotFoundException extends DomainException {

  public UserNotFoundException(String message) {
    super(message);
  }

  public static UserNotFoundException forUser(EntityId userId) {
    return new UserNotFoundException("User not registered: " + userId.getValue());
  }
}
