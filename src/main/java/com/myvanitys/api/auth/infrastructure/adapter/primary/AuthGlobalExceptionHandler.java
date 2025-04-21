package com.myvanitys.api.auth.infrastructure.adapter.primary;

import java.net.URI;

import com.myvanitys.api.model.v1.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthGlobalExceptionHandler {

  private static final URI VALIDATION_ERROR_TYPE = URI.create("https://api.myvanitys.com/problems/validation-error");

  private static final URI INTERNAL_ERROR_TYPE = URI.create("https://api.myvanitys.com/problems/internal-error");

  private static final URI AUTH_GOOGLE_INSTANCE = URI.create("/api/v1/auth/google");

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Validation Error")
        .status(400)
        .detail("Invalid input data: " + ex.getMessage())
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(MissingRequestHeaderException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Missing Required Header")
        .status(400)
        .detail("Required header is missing: " + ex.getHeaderName())
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ProblemDetail> handleRuntimeExceptions(RuntimeException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Internal Server Error")
        .status(500)
        .detail("Authentication failed: " + ex.getMessage())
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}