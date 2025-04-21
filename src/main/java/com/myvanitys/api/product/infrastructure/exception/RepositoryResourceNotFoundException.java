package com.myvanitys.api.product.infrastructure.exception;

import com.myvanitys.api.common.InfrastructureException;

public class RepositoryResourceNotFoundException extends InfrastructureException {

  public RepositoryResourceNotFoundException(String message) {
    super(message);
  }

  public RepositoryResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public static RepositoryResourceNotFoundException notFound(String entityType, Object id) {
    return new RepositoryResourceNotFoundException(entityType + " not found with ID: " + id);
  }

}
