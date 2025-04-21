package com.myvanitys.api.common;
import com.myvanitys.api.model.v1.ProblemDetail;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
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
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Missing Required Header", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Authorization"));
    }

    @Test
    void handleProductNotFoundException_DebeRetornarBadRequest() {
        // Arrange
        ProductNotFoundException ex = new ProductNotFoundException("Producto no encontrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
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
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Domain validation error", response.getBody().getTitle());
    }

    @Test
    void handleDatabaseException_DebeRetornarBadRequest() {
        // Arrange
        DatabaseException ex = new DatabaseException("Error de base de datos");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Infrastructure validation error", response.getBody().getTitle());
    }

    @Test
    void handleRepositoryResourceNotFoundException_DebeRetornarBadRequest() {
        // Arrange
        RepositoryResourceNotFoundException ex = new RepositoryResourceNotFoundException("Recurso no encontrado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Infrastructure validation error", response.getBody().getTitle());
    }

    @Test
    void handleUnauthorizedException_DebeRetornarBadRequest() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("No autorizado");

        // Act
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingHeaderExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Infrastructure validation error", response.getBody().getTitle());
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
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Error interno"));
    }

}