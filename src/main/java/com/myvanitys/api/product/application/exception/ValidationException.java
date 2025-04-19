package com.myvanitys.api.product.application.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.myvanitys.api.common.ApplicationException;

/**
 * Exception thrown when input validation fails in use cases
 */
public class ValidationException extends ApplicationException {

  private final List<ValidationError> errors;

  public ValidationException(String message, List<ValidationError> errors) {
    super(message);
    this.errors = new ArrayList<>(errors);
  }

  public ValidationException(String message, ValidationError error) {
    super(message);
    this.errors = Collections.singletonList(error);
  }

  public List<ValidationError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public static ValidationException withError(String field, String message) {
    ValidationError error = new ValidationError(field, message);
    return new ValidationException("Validation failed for field: " + field, error);
  }

  public static ValidationException withErrors(List<ValidationError> errors) {
    return new ValidationException("Multiple validation errors occurred", errors);
  }

  /**
   * Represents specific validation error
   */
  public static class ValidationError {

    private final String field;

    private final String message;

    public ValidationError(String field, String message) {
      this.field = field;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public String getMessage() {
      return message;
    }
  }
}
