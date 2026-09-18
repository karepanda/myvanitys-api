package com.myvanitys.api.common;

import com.myvanitys.api.auth.domain.exception.AuthenticationFailedException;
import com.myvanitys.api.auth.domain.exception.GoogleAuthException;
import com.myvanitys.api.auth.domain.exception.UserAlreadyExistsException;
import com.myvanitys.api.auth.domain.exception.UserNotFoundException;
import com.myvanitys.api.model.v1.ProblemDetail;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleMethodArgumentNotValidException_DebeRetornarBadRequest() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getMessage()).thenReturn("error de validación");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("error de validación"));
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
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Missing Required Header", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Authorization"));
    }

    @Test
    void handleProductNotFoundException_DebeRetornarNotFound() {
        // Arrange
        ProductNotFoundException ex = new ProductNotFoundException("Producto no encontrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Product Not Found", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Producto no encontrado"));
    }

    @Test
    void handleProductValidationException_DebeRetornarBadRequest() {
        // Arrange
        ProductValidationException ex = new ProductValidationException("Error de validación del producto");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Domain validation error", response.getBody().getTitle());
    }

    @Test
    void handleDatabaseException_DebeRetornarInternalServerError() {
        // Arrange
        DatabaseException ex = new DatabaseException("Error de base de datos");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Infrastructure validation error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Error de base de datos"));
    }

    @Test
    void handleRepositoryResourceNotFoundException_DebeRetornarInternalServerError() {
        // Arrange
        RepositoryResourceNotFoundException ex = new RepositoryResourceNotFoundException("Recurso no encontrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Infrastructure validation error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Recurso no encontrado"));
    }

    @Test
    void handleUnauthorizedException_DebeRetornarUnauthorized() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("No autorizado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Infrastructure validation error", response.getBody().getTitle());
    }

    @Test
    void handleGoogleAuthException_DebeRetornarNotFound() {
        // Arrange
        GoogleAuthException ex = new GoogleAuthException("Error de autorización con Google");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Google Authorization Error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Error de autorización con Google"));
    }

    @Test
    void handleRuntimeException_DebeRetornarInternalServerError() {
        // Arrange
        RuntimeException ex = new RuntimeException("Error interno");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleRuntimeExceptions(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Error interno"));
    }

    @Test
    void handleUserNotFoundException_DebeRetornarUnauthorized() {
        // Arrange
        UserNotFoundException ex = new UserNotFoundException("Usuario no registrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Authorization Error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Usuario no registrado"));
    }

    @Test
    void handleUserAlreadyExistsException_DebeRetornarConflict() {
        // Arrange
        UserAlreadyExistsException ex = new UserAlreadyExistsException("El usuario ya existe");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleUserAlreadyExistsException(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(409, response.getBody().getStatus());
        assertEquals("User Already Exists", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("El usuario ya existe"));
    }

    @Test
    void handleAuthenticationFailedException_DebeRetornarUnauthorized() {
        // Arrange
        AuthenticationFailedException ex = new AuthenticationFailedException("Credenciales inválidas");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleAuthenticationFailedException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertHttpStatusMatchesBodyStatus(response);
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Authentication Error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Credenciales inválidas"));
    }

    private void assertHttpStatusMatchesBodyStatus(ResponseEntity<ProblemDetail> response) {
        assertNotNull(response.getBody(), "Response body must not be null");
        assertEquals(
            response.getStatusCode().value(),
            response.getBody().getStatus(),
            "HTTP status must match ProblemDetail.status"
        );
    }

}
