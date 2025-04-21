package com.myvanitys.api.product.infrastructure.exception;

import com.myvanitys.api.common.InfrastructureException;

/**
 * Exception thrown when database-related issues occur.
 */
public class DatabaseException extends InfrastructureException {

  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public static DatabaseException connectionError(Throwable cause) {
    return new DatabaseException("Error connecting to database", cause);
  }

  public static DatabaseException queryError(String query, Throwable cause) {
    return new DatabaseException("Error executing query: " + query, cause);
  }
}
