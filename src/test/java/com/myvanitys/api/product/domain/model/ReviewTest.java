package com.myvanitys.api.product.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewTest {

  private Review review1;

  private Product product;

  private EntityId userId;

  private EntityId productId;

  @BeforeEach
  void setUp() {
    EntityId reviewId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    productId = new EntityId(UUID.randomUUID());

    Category category = new Category(new EntityId(UUID.randomUUID()), "Skincare");
    product = new Product(productId, "Serum", "Marca X", category, "#AABBCC");

    review1 = new Review(reviewId, userId, product, 5, "Excellent product");
  }

  @Test
  void testReviewInitialization() {
    assertNotNull(review1.getId());
    assertEquals(5, review1.getRating());
    assertEquals("Excellent product", review1.getComment());
  }

  @Test
  void testEqualsAndHashCode() {
    Review review2 = new Review(review1.getId(), review1.getUserId(), review1.getProduct(), 5, "Excellent product");
    assertEquals(review1, review2);
    assertEquals(review1.hashCode(), review2.hashCode());
  }

  @Test
  void testDifferentReviewsNotEqual() {
    Review review2 = new Review(new EntityId(UUID.randomUUID()), review1.getUserId(), product, 4, "Good product");
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
    ProductUserRelation relation = new ProductUserRelation(new EntityId(UUID.randomUUID()), productId, userId);
    Review review = Review.createFromRelation(new EntityId(UUID.randomUUID()), relation, 3, "Acceptable", product);
    assertNotNull(review.getId());
    assertEquals(userId, review.getUserId());
    assertEquals(3, review.getRating());
    assertEquals("Acceptable", review.getComment());
  }

}