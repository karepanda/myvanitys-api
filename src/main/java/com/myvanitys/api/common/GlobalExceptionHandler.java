package com.myvanitys.api.common;

import com.myvanitys.api.auth.domain.exception.AuthenticationFailedException;
import com.myvanitys.api.auth.domain.exception.GoogleAuthException;
import com.myvanitys.api.auth.domain.exception.UserAlreadyExistsException;
import com.myvanitys.api.auth.domain.exception.UserNotFoundException;
import com.myvanitys.api.auth.domain.exception.UserRegistrationFailedException;
import com.myvanitys.api.common.config.CorrelationIdFilter;
import com.myvanitys.api.model.v1.ProblemDetail;
import com.myvanitys.api.product.domain.exception.ProductAlreadyInVanityException;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final URI VALIDATION_ERROR_TYPE = URI.create("https://api.myvanitys.com/problems/validation-error");

  private static final URI INTERNAL_ERROR_TYPE = URI.create("https://api.myvanitys.com/problems/internal-error");

  private static final URI AUTH_GOOGLE_INSTANCE = URI.create("/api/auth/google");

  private static final URI MYVANITYS_INSTANCE = URI.create("myvanitys/api/");

  private static final URI PRODUCT_INSTANCE = URI.create("myvanitys/api/products/");

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationExceptions() {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Validation Error")
        .status(400)
        .detail("Invalid input data")
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

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(ProductNotFoundException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Product Not Found")
        .status(404)
        .detail("Product failed " + ex.getMessage())
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(ProductValidationException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(ProductValidationException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Domain validation error")
        .status(400)
        .detail("Product failed " + ex.getMessage())
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ProblemDetail> handleApplicationValidationException(ValidationException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Validation Error")
        .status(400)
        .detail("Validation failed: " + formatValidationErrors(ex))
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(ReviewValidationException.class)
  public ResponseEntity<ProblemDetail> handleReviewValidationException(ReviewValidationException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Review Validation Error")
        .status(400)
        .detail(ex.getMessage())
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(ProductAlreadyInVanityException.class)
  public ResponseEntity<ProblemDetail> handleProductAlreadyInVanityException(ProductAlreadyInVanityException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(VALIDATION_ERROR_TYPE)
        .title("Product Already In Vanity")
        .status(409)
        .detail(ex.getMessage())
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(DatabaseException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(DatabaseException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Infrastructure validation error")
        .status(500)
        .detail("Product failed " + ex.getMessage())
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  @ExceptionHandler(RepositoryResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(RepositoryResourceNotFoundException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Infrastructure validation error")
        .status(500)
        .detail("Product failed " + ex.getMessage())
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorizedException() {
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Infrastructure validation error")
        .status(401)
        .detail("Authentication is required to access this resource")
        .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(GoogleAuthException.class)
  public ResponseEntity<ProblemDetail> handleGoogleAuthException() {
    ProblemDetail problem = new ProblemDetail()
        .type(AUTH_GOOGLE_INSTANCE)
        .title("Google Authorization Error")
        .status(404)
        .detail("Google authentication failed")
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ProblemDetail> handleRuntimeExceptions(RuntimeException ex) {
    log.error("Unhandled exception. type={}, requestId={}",
        ex.getClass().getName(), MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Internal Server Error")
        .status(500)
        .detail("An unexpected error occurred")
        .instance(MYVANITYS_INSTANCE);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleUserNotFoundException() {
    ProblemDetail problem = new ProblemDetail()
        .type(AUTH_GOOGLE_INSTANCE)
        .title("Authorization Error")
        .status(401)
        .detail("User not registered in the system")
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleUserAlreadyExistsException() {
    ProblemDetail problem = new ProblemDetail()
        .type(AUTH_GOOGLE_INSTANCE)
        .title("User Already Exists")
        .status(409)  // Conflict
        .detail("User already exists")
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(UserRegistrationFailedException.class)
  public ResponseEntity<ProblemDetail> handleUserRegistrationFailedException() {
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Internal Server Error")
        .status(500)
        .detail("User registration failed")
        .instance(MYVANITYS_INSTANCE);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ProblemDetail> handleAuthenticationFailedException() {
    ProblemDetail problem = new ProblemDetail()
        .type(AUTH_GOOGLE_INSTANCE)
        .title("Authentication Error")
        .status(401)  // Unauthorized
        .detail("Authentication failed")
        .instance(AUTH_GOOGLE_INSTANCE);

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  private static String formatValidationErrors(ValidationException ex) {
    return ex.getErrors().stream()
        .map(error -> error.field() + ": " + error.message())
        .collect(Collectors.joining("; "));
  }

}
