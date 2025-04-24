package com.myvanitys.api.product.domain.model;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewTest {

  private Review review1;

  private ProductUserRelation productUserRelation;

  private EntityId userId;

  private EntityId productUserId;

    @BeforeEach
  void setUp() {
        EntityId reviewId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    EntityId productId = new EntityId(UUID.randomUUID());
    productUserId = new EntityId(UUID.randomUUID());
    // Assuming ProductUserRelation is a class that holds the relationship between product and user
    productUserRelation = new ProductUserRelation( productUserId = new EntityId(UUID.randomUUID()), productId, userId, reviewId);

    review1 = new Review(reviewId, userId, productUserId, 5, "Excellent product");
  }

  @Test
  void testReviewInitialization() {
    assertNotNull(review1.getId());
    assertEquals(userId, review1.getUserId());
    assertEquals(productUserRelation.getId(), review1.getProductUserEntity());
    assertEquals(5, review1.getRating());
    assertEquals("Excellent product", review1.getComment());
  }

  @Test
  void testEqualsAndHashCode() {
    Review review2 = new Review(review1.getId(), review1.getUserId(), review1.getProductUserEntity(), 5, "Different comment");
    assertEquals(review1, review2);  // Equality is based only on ID
    assertEquals(review1.hashCode(), review2.hashCode());
  }

  @Test
  void testDifferentReviewsNotEqual() {
    Review review2 = new Review(new EntityId(UUID.randomUUID()), userId, productUserId, 4, "Good product");
    assertNotEquals(review1, review2);
  }

  @Test
  void testCreateReview() {
    Review review = Review.create(userId, productUserId, 4, "Good product");
    assertNotNull(review.getId());
    assertEquals(userId, review.getUserId());
    assertEquals(productUserRelation.getId(), review.getProductUserEntity());
    assertEquals(4, review.getRating());
    assertEquals("Good product", review.getComment());
  }

  @Test
  void testValidateRatingThrowsValidationExceptionForInvalidValues() {
    assertThrows(ValidationException.class, () ->
        Review.create(userId, productUserId, 0, "Invalid rating"));

    assertThrows(ValidationException.class, () ->
        Review.create(userId, productUserId, 6, "Invalid rating"));
        
    ValidationException exception = assertThrows(ValidationException.class, () ->
        Review.create(userId, productUserId, -1, "Invalid rating"));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("rating") &&
            error.message().equals("Rating must be between 1 and 5")));
  }

  @Test
  void testValidateCommentThrowsValidationExceptionForEmptyComment() {
    // Prueba string vacío
    ValidationException exception = assertThrows(ValidationException.class, () ->
        Review.create(userId, productUserId, 5, ""));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("comment") &&
            error.message().equals("Comment cannot be empty")));

    // Prueba string con espacios en blanco
    exception = assertThrows(ValidationException.class, () ->
        Review.create(userId, productUserId, 5, "   "));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("comment") &&
            error.message().equals("Comment cannot be empty")));
  }

  @Test
  void testCreateReviewWithNullValues() {
    assertThrows(NullPointerException.class, () ->
        Review.create(null, productUserId, 5, "Valid comment"));

    assertThrows(NullPointerException.class, () ->
        Review.create(userId, null, 5, "Valid comment"));

    assertThrows(NullPointerException.class, () ->
        Review.create(userId, productUserId, 5, null));
  }

  @Test
  void testUpdateDetailsWithInvalidValues() {
    Review review = Review.create(userId, productUserId, 5, "Initial comment");

    // Prueba rating inválido
    ValidationException exception = assertThrows(ValidationException.class, () ->
        review.updateDetails(0, "Valid comment"));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("rating") &&
            error.message().equals("Rating must be between 1 and 5")));

    // Prueba comentario inválido
    exception = assertThrows(ValidationException.class, () ->
        review.updateDetails(4, ""));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("comment") &&
            error.message().equals("Comment cannot be empty")));

    // Verificar que los valores originales no cambiaron
    assertEquals(5, review.getRating());
    assertEquals("Initial comment", review.getComment());
  }

  @Test 
  void testCreateWithValidRatingBoundaryValues() {
    // Prueba valores límite válidos (1 y 5)
    Review review1 = Review.create(userId, productUserId, 1, "Valid comment");
    assertEquals(1, review1.getRating());

    Review review5 = Review.create(userId, productUserId, 5, "Valid comment");
    assertEquals(5, review5.getRating());
  }

  @Test
  void testUpdateDetails() {
    review1.updateDetails(4, "Updated comment");

    assertEquals(4, review1.getRating());
    assertEquals("Updated comment", review1.getComment());
  }

  @Test
  void testUpdateDetailsValidatesRating() {
    ValidationException exception = assertThrows(ValidationException.class, () ->
        review1.updateDetails(0, "Should throw exception"));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("rating") &&
            error.message().equals("Rating must be between 1 and 5")));
  }
}