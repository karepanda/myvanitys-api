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

  private Product product;

  private EntityId userId;

  private EntityId productId;

  private EntityId reviewId;

  @BeforeEach
  void setUp() {
    reviewId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    productId = new EntityId(UUID.randomUUID());

    Category category = new Category(new EntityId(UUID.randomUUID()), "Skincare");
    product = new Product(productId, "Serum", "Marca X", category, "#AABBCC");

    review1 = new Review(reviewId, userId, product, 5, "Excellent product");
  }

  @Test
  void testReviewInitialization() {
    assertNotNull(review1.getId());
    assertEquals(userId, review1.getUserId());
    assertEquals(product, review1.getProduct());
    assertEquals(5, review1.getRating());
    assertEquals("Excellent product", review1.getComment());
  }

  @Test
  void testEqualsAndHashCode() {
    Review review2 = new Review(review1.getId(), review1.getUserId(), review1.getProduct(), 5, "Different comment");
    assertEquals(review1, review2);  // Equality is based only on ID
    assertEquals(review1.hashCode(), review2.hashCode());
  }

  @Test
  void testDifferentReviewsNotEqual() {
    Review review2 = new Review(new EntityId(UUID.randomUUID()), userId, product, 4, "Good product");
    assertNotEquals(review1, review2);
  }

  @Test
  void testCreateReview() {
    Review review = Review.create(userId, product, 4, "Good product");
    assertNotNull(review.getId());
    assertEquals(userId, review.getUserId());
    assertEquals(product, review.getProduct());
    assertEquals(4, review.getRating());
    assertEquals("Good product", review.getComment());
  }

  @Test
  void testCreateReviewFromRelation() {
    ProductUserRelation relation = new ProductUserRelation(
        new EntityId(UUID.randomUUID()),
        productId,
        userId,
        null  // No review associated yet
    );

    Review review = Review.createFromRelation(reviewId, relation, 3, "Acceptable", product);

    assertNotNull(review.getId());
    assertEquals(reviewId, review.getId());
    assertEquals(userId, review.getUserId());
    assertEquals(product, review.getProduct());
    assertEquals(3, review.getRating());
    assertEquals("Acceptable", review.getComment());
  }

  @Test
  void testValidateRatingThrowsValidationExceptionForInvalidValues() {
    ValidationException exception = assertThrows(ValidationException.class, () ->
        new Review(reviewId, userId, product, 0, "Invalid rating"));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("rating") &&
            error.message().equals("Rating must be between 1 and 5")));

    exception = assertThrows(ValidationException.class, () ->
        new Review(reviewId, userId, product, 6, "Invalid rating"));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("rating") &&
            error.message().equals("Rating must be between 1 and 5")));
  }

  @Test
  void testValidateCommentThrowsValidationExceptionForEmptyComment() {
    ValidationException exception = assertThrows(ValidationException.class, () ->
        new Review(reviewId, userId, product, 5, ""));
    assertTrue(exception.getErrors().stream()
        .anyMatch(error -> error.field().equals("comment") &&
            error.message().equals("Comment cannot be empty")));
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