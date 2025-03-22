package com.myvanitys.api.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewTest {

  private Review review1;

  private User user;

  private Product product;

  @BeforeEach
  void setUp() {
    EntityId reviewId = new EntityId(UUID.randomUUID());
    EntityId userId = new EntityId(UUID.randomUUID());
    EntityId productId = new EntityId(UUID.randomUUID());

    Category category = new Category(new EntityId(UUID.randomUUID()), "Skincare");
    product = new Product(productId, "Serum", "Marca X", category, "#AABBCC");

    review1 = new Review(reviewId, userId, product, 5, "Excelente producto");
  }

  @Test
  void testReviewInitialization() {
    assertNotNull(review1.getId());
    assertEquals(5, review1.getRating());
    assertEquals("Excelente producto", review1.getDescription());
  }

  @Test
  void testToString() {
    String expected = "Review{id=" + review1.getId() + ", user=" + review1.getUserId() +
        ", product=" + review1.getProduct() + ", rating=5, description='Excelente producto'}";
    assertEquals(expected, review1.toString());
  }

  @Test
  void testEqualsAndHashCode() {
    Review review2 = new Review(review1.getId(), review1.getUserId(), review1.getProduct(), 5, "Excelente producto");
    assertEquals(review1, review2);
    assertEquals(review1.hashCode(), review2.hashCode());
  }

  @Test
  void testDifferentReviewsNotEqual() {
    Review review2 = new Review(new EntityId(UUID.randomUUID()), review1.getUserId(), product, 4, "Buen producto");
    assertNotEquals(review1, review2);
  }
}