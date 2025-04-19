package com.myvanitys.api.product.infrastructure.adapter.primary;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.myvanitys.api.common.ApplicationException;
import com.myvanitys.api.common.DomainException;
import com.myvanitys.api.common.InfrastructureException;
import com.myvanitys.api.product.application.exception.ValidationException;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the REST API.
 */
@ControllerAdvice
public class ProductGlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(ObjectError::getDefaultMessage)
        .toList();

    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles domain-specific exceptions.
   */
  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<Object> handleProductNotFoundException(ProductNotFoundException ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        ex.getMessage()
    );

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles generic domain exceptions.
   */
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<Object> handleDomainException(DomainException ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.BAD_REQUEST.value(),
        "Domain Rule Violation",
        ex.getMessage()
    );

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles input validation exceptions.
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Object> handleValidationException(ValidationException ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.BAD_REQUEST.value(),
        "Validation Error",
        ex.getMessage()
    );

    // Include validation error details in the response body
    body.put("validationErrors", ex.getErrors().stream()
        .map(error -> Map.of(
            "field", error.getField(),
            "message", error.getMessage()
        ))
        .collect(Collectors.toList()));

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles generic application-level exceptions.
   */
  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<Object> handleApplicationException(ApplicationException ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.BAD_REQUEST.value(),
        "Application Error",
        ex.getMessage()
    );

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles database-related exceptions.
   */
  @ExceptionHandler(DatabaseException.class)
  public ResponseEntity<Object> handleDatabaseException(DatabaseException ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Database Error",
        "An error occurred while accessing the database"
    );

    // Do not expose internal exception details to the client for security reasons
    // Log full error details on the server only

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles generic infrastructure-level exceptions.
   */
  @ExceptionHandler(InfrastructureException.class)
  public ResponseEntity<Object> handleInfrastructureException(InfrastructureException ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "System Error",
        "An internal system error occurred"
    );

    // Do not expose internal exception details to the client for security reasons
    // Log full error details on the server only

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles any uncaught exceptions.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGenericException(Exception ex) {
    Map<String, Object> body = createErrorBody(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Unexpected Error",
        "An unexpected error occurred"
    );

    // Do not expose internal exception details to the client for security reasons
    // Log full error details on the server only

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Utility method for creating a consistent error response body.
   */
  private Map<String, Object> createErrorBody(int status, String error, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status);
    body.put("error", error);
    body.put("message", message);
    return body;
  }
}
