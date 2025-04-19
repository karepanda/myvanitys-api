package com.myvanitys.api.common;

/**
 * Base class for all infrastructure exceptions These exceptions represent errors in external components, persistence, communication with
 * services, etc.
 */
public abstract class InfrastructureException extends RuntimeException {

  protected InfrastructureException(String message) {
    super(message);
  }

  protected InfrastructureException(String message, Throwable cause) {
    super(message, cause);
  }
}