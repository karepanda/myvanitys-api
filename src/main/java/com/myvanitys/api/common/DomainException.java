package com.myvanitys.api.common;

/**
 * Base class for all domain exceptions These exceptions represent violations of business rules or exceptional situations in the domain
 * context
 */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}