package com.myvanitys.api.common;

import com.myvanitys.api.common.config.CorrelationIdFilter;
import com.myvanitys.api.model.v1.ProblemDetail;
import com.myvanitys.api.product.domain.exception.ProductAlreadyInVanityException;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {
    private static final URI VALIDATION_ERROR_TYPE =
        URI.create("https://api.myvanitys.com/problems/validation-error");
    private static final URI PRODUCT_INSTANCE = URI.create("myvanitys/api/products/");

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleMethodArgumentNotValidException_DebeRetornarBadRequest() {
        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleValidationExceptions();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(400, body.getStatus());
        assertEquals("Validation Error", body.getTitle());
        assertEquals("Invalid input data", body.getDetail());
    }

    @Test
    void handleMissingRequestHeaderException_DebeRetornarBadRequest() {
        // Arrange
        MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
        when(ex.getHeaderName()).thenReturn("Authorization");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(400, body.getStatus());
        assertEquals("Missing Required Header", body.getTitle());
        assertDetailContains(body, "Authorization");
    }

    @Test
    void handleProductNotFoundException_DebeRetornarNotFound() {
        // Arrange
        ProductNotFoundException ex = new ProductNotFoundException("Producto no encontrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(404, body.getStatus());
        assertEquals("Product Not Found", body.getTitle());
        assertDetailContains(body, "Producto no encontrado");
    }

    @Test
    void handleProductValidationException_DebeRetornarBadRequest() {
        // Arrange
        ProductValidationException ex = new ProductValidationException("Error de validación del producto");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(400, body.getStatus());
        assertEquals("Domain validation error", body.getTitle());
    }

    @Test
    void handleDatabaseException_DebeRetornarInternalServerError() {
        // Arrange
        DatabaseException ex = new DatabaseException("Error de base de datos");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(500, body.getStatus());
        assertEquals("Infrastructure validation error", body.getTitle());
        assertDetailContains(body, "Error de base de datos");
    }

    @Test
    void handleRepositoryResourceNotFoundException_DebeRetornarInternalServerError() {
        // Arrange
        RepositoryResourceNotFoundException ex = new RepositoryResourceNotFoundException("Recurso no encontrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(500, body.getStatus());
        assertEquals("Infrastructure validation error", body.getTitle());
        assertDetailContains(body, "Recurso no encontrado");
    }

    @Test
    void handleUnauthorizedException_DebeRetornarUnauthorized() {
        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleUnauthorizedException();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(401, body.getStatus());
        assertEquals("Infrastructure validation error", body.getTitle());
    }

    @Test
    void handleGoogleAuthException_DebeRetornarNotFound() {
        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleGoogleAuthException();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(404, body.getStatus());
        assertEquals("Google Authorization Error", body.getTitle());
        assertEquals("Google authentication failed", body.getDetail());
        assertDetailDoesNotContain(body, "Error de autorización con Google");
    }

    @Test
    void handleRuntimeException_DebeRetornarInternalServerError(CapturedOutput output) {
        // Arrange
        String secret = "synthetic-secret-runtime-message";
        String requestId = UUID.randomUUID().toString();
        RuntimeException ex = new RuntimeException(secret);

        // Act
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, requestId);
        ResponseEntity<ProblemDetail> response;
        try {
            response = exceptionHandler.handleRuntimeExceptions(ex);
        } finally {
            MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        }

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getTitle());
        assertEquals("An unexpected error occurred", body.getDetail());
        assertDetailDoesNotContain(body, secret);
        assertTrue(output.getAll().contains(RuntimeException.class.getName()));
        assertTrue(output.getAll().contains(requestId));
        assertFalse(output.getAll().contains(secret));
    }

    @Test
    void handleUserNotFoundException_DebeRetornarUnauthorized() {
        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleUserNotFoundException();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(401, body.getStatus());
        assertEquals("Authorization Error", body.getTitle());
        assertEquals("User not registered in the system", body.getDetail());
        assertDetailDoesNotContain(body, "Usuario no registrado");
    }

    @Test
    void handleUserAlreadyExistsException_DebeRetornarConflict() {
        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleUserAlreadyExistsException();

        // Assert
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    ProblemDetail body = assertAndGetConsistentBody(response);
    assertEquals(409, body.getStatus());
    assertEquals("User Already Exists", body.getTitle());
    assertEquals("User already exists", body.getDetail());
    assertDetailDoesNotContain(body, "El usuario ya existe");
  }

  @Test
  void handleUserRegistrationFailedException_DebeRetornarInternalServerError() {
    // Arrange
    String secret = "synthetic-registration-secret";

    // Act
    ResponseEntity<ProblemDetail> response = exceptionHandler.handleUserRegistrationFailedException();

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ProblemDetail body = assertAndGetConsistentBody(response);
    assertEquals(500, body.getStatus());
    assertEquals("Internal Server Error", body.getTitle());
    assertEquals("User registration failed", body.getDetail());
    assertDetailDoesNotContain(body, secret);
  }

    @Test
    void handleAuthenticationFailedException_DebeRetornarUnauthorized() {
        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleAuthenticationFailedException();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(401, body.getStatus());
        assertEquals("Authentication Error", body.getTitle());
        assertEquals("Authentication failed", body.getDetail());
        assertDetailDoesNotContain(body, "Credenciales inválidas");
    }

    @Test
    void handleApplicationValidationException_DebeRetornarBadRequestConErroresDeCampo() {
        // Arrange
        UUID categoryId = UUID.fromString("33333333-0000-0000-0000-000000000001");
        ValidationException ex = ValidationException.withError(
            "categoryId", "Category not found with ID: " + categoryId);

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleApplicationValidationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(400, body.getStatus());
        assertEquals("Validation Error", body.getTitle());
        assertEquals(VALIDATION_ERROR_TYPE, body.getType());
        assertDetailContains(body, "categoryId", "Category not found with ID: " + categoryId);
        assertEquals(PRODUCT_INSTANCE, body.getInstance());
    }

    @Test
    void handleApplicationValidationException_whenMultipleErrors_thenIncludesEveryFieldAndMessage() {
        // Arrange
        ValidationException ex = ValidationException.withErrors(java.util.List.of(
            new ValidationException.ValidationError("name", "Product name is required and cannot be empty"),
            new ValidationException.ValidationError("colorHex", "Color is required and cannot be empty")
        ));

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleApplicationValidationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(400, body.getStatus());
        assertDetailContains(
            body,
            "name",
            "Product name is required and cannot be empty",
            "colorHex",
            "Color is required and cannot be empty"
        );
    }

    @Test
    void handleReviewValidationException_DebeRetornarBadRequest() {
        // Arrange
        ReviewValidationException ex = new ReviewValidationException("Rating must be between 1 and 5");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleReviewValidationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(400, body.getStatus());
        assertEquals("Review Validation Error", body.getTitle());
        assertEquals(VALIDATION_ERROR_TYPE, body.getType());
        assertDetailContains(body, "Rating must be between 1 and 5");
        assertEquals(PRODUCT_INSTANCE, body.getInstance());
    }

    @Test
    void handleProductAlreadyInVanityException_DebeRetornarConflict() {
        // Arrange
        ProductAlreadyInVanityException ex =
            new ProductAlreadyInVanityException("Product is already associated with the user");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleProductAlreadyInVanityException(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail body = assertAndGetConsistentBody(response);
        assertEquals(409, body.getStatus());
        assertEquals("Product Already In Vanity", body.getTitle());
        assertEquals(VALIDATION_ERROR_TYPE, body.getType());
        assertDetailContains(body, "Product is already associated with the user");
        assertEquals(PRODUCT_INSTANCE, body.getInstance());
    }

    private ProblemDetail assertAndGetConsistentBody(ResponseEntity<ProblemDetail> response) {
        ProblemDetail body = response.getBody();
        assertNotNull(body, "Response body must not be null");
        assertEquals(
            response.getStatusCode().value(),
            body.getStatus(),
            "HTTP status must match ProblemDetail.status"
        );
        return body;
    }

    private void assertDetailContains(ProblemDetail body, String... expectedFragments) {
        String detail = body.getDetail();
        assertNotNull(detail, "Problem detail must not be null");

        for (String expected : expectedFragments) {
            assertTrue(
                detail.contains(expected),
                () -> "Expected detail to contain: " + expected + ", but was: " + detail
            );
        }
    }

    private void assertDetailDoesNotContain(ProblemDetail body, String unexpectedFragment) {
        String detail = body.getDetail();
        assertNotNull(detail, "Problem detail must not be null");
        assertFalse(
            detail.contains(unexpectedFragment),
            () -> "Expected detail not to contain: " + unexpectedFragment + ", but was: " + detail
        );
    }

}
