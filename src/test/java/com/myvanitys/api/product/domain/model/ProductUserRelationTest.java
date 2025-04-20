package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductUserRelationTest {

  private EntityId id;

  private EntityId productId;

  private EntityId userId;

  private EntityId reviewId;

  private ProductUserRelation relation;

  @BeforeEach
  void setUp() {
    id = new EntityId(UUID.randomUUID());
    productId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    reviewId = new EntityId(UUID.randomUUID());
    relation = new ProductUserRelation(id, productId, userId, null);  // Se agrega null para reviewId
  }

  @Test
  void shouldInitializeFieldsCorrectly() {
    assertThat(relation.getId()).isEqualTo(id);
    assertThat(relation.getProductId()).isEqualTo(productId);
    assertThat(relation.getUserId()).isEqualTo(userId);
    assertThat(relation.getReviewId()).isNull();
  }

  @Test
  void shouldInitializeWithReviewId() {
    ProductUserRelation relationWithReview = new ProductUserRelation(id, productId, userId, reviewId);

    assertThat(relationWithReview.getId()).isEqualTo(id);
    assertThat(relationWithReview.getProductId()).isEqualTo(productId);
    assertThat(relationWithReview.getUserId()).isEqualTo(userId);
    assertThat(relationWithReview.getReviewId()).isEqualTo(reviewId);
  }

  @Test
  void shouldSetReviewId() {
    assertThat(relation.getReviewId()).isNull();

    relation.setReviewId(reviewId);

    assertThat(relation.getReviewId()).isEqualTo(reviewId);
  }

  @Test
  void equalsAndHashCodeShouldWorkBasedOnUserIdAndProductId() {
    ProductUserRelation sameRelation = new ProductUserRelation(
        new EntityId(UUID.randomUUID()), productId, userId, new EntityId(UUID.randomUUID()));
    ProductUserRelation differentRelation = new ProductUserRelation(
        new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()), null);

    assertThat(relation)
        .isEqualTo(sameRelation)
        .hasSameHashCodeAs(sameRelation);

    assertThat(relation)
        .isNotEqualTo(differentRelation);
  }

  @Test
  void shouldNotBeEqualToNullOrDifferentClass() {
    assertThat(relation).isNotEqualTo(null);
    assertThat(relation).isNotEqualTo("Some String");
  }

  @Test
  void shouldThrowExceptionWhenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> new ProductUserRelation(null, productId, userId, reviewId))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> new ProductUserRelation(id, null, userId, reviewId))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> new ProductUserRelation(id, productId, null, reviewId))
        .isInstanceOf(NullPointerException.class);
  }
}