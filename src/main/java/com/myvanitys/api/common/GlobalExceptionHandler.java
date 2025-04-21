package com.myvanitys.api.common;

import com.myvanitys.api.model.v1.ProblemDetail;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final URI VALIDATION_ERROR_TYPE = URI.create("https://api.myvanitys.com/problems/validation-error");

  private static final URI INTERNAL_ERROR_TYPE = URI.create("https://api.myvanitys.com/problems/internal-error");

  private static final URI AUTH_GOOGLE_INSTANCE = URI.create("/api/auth/google");

  private static final URI MYVANITYS_INSTANCE = URI.create("myvanitys/api/");

  private static final URI PRODUCT_INSTANCE = URI.create("myvanitys/api/products/");

  private static final String MYVANITYS_API_FAILED = "My vanitys API failed";


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


  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(ProductNotFoundException ex) {
    ProblemDetail problem = new ProblemDetail()
            .type(VALIDATION_ERROR_TYPE)
            .title("Product Not Found")
            .status(404)
            .detail("Product failed " + ex.getMessage())
            .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
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

  @ExceptionHandler(DatabaseException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(DatabaseException ex) {
    ProblemDetail problem = new ProblemDetail()
            .type(INTERNAL_ERROR_TYPE)
            .title("Infrastructure validation error")
            .status(500)
            .detail("Product failed " + ex.getMessage())
            .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(RepositoryResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(RepositoryResourceNotFoundException ex) {
    ProblemDetail problem = new ProblemDetail()
            .type(INTERNAL_ERROR_TYPE)
            .title("Infrastructure validation error")
            .status(500)
            .detail("Product failed " + ex.getMessage())
            .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeaderExceptions(UnauthorizedException ex) {
    ProblemDetail problem = new ProblemDetail()
            .type(INTERNAL_ERROR_TYPE)
            .title("Infrastructure validation error")
            .status(401)
            .detail("Token verification failed " + ex.getMessage())
            .instance(PRODUCT_INSTANCE);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ProblemDetail> handleRuntimeExceptions(RuntimeException ex) {
    ProblemDetail problem = new ProblemDetail()
        .type(INTERNAL_ERROR_TYPE)
        .title("Internal Server Error")
        .status(500)
        .detail(MYVANITYS_API_FAILED + ex.getMessage())
        .instance(MYVANITYS_INSTANCE);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}