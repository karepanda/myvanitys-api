package com.myvanitys.api.product.infrastructure.exception;

import com.myvanitys.api.common.InfrastructureException;

public class UnauthorizedException extends InfrastructureException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

  public static UnauthorizedException connectionError(Throwable cause) {
    return new UnauthorizedException("Error connecting to database", cause);
  }

  public static UnauthorizedException queryError(String query, Throwable cause) {
    return new UnauthorizedException("Error executing query: " + query, cause);
  }

}
