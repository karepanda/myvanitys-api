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

    // Usar reconstruct en lugar del constructor
    relation = ProductUserRelation.reconstruct(id, productId, userId, null);
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
    // Usar reconstruct en lugar del constructor
    ProductUserRelation relationWithReview = ProductUserRelation.reconstruct(id, productId, userId, reviewId);

    assertThat(relationWithReview.getId()).isEqualTo(id);
    assertThat(relationWithReview.getProductId()).isEqualTo(productId);
    assertThat(relationWithReview.getUserId()).isEqualTo(userId);
    assertThat(relationWithReview.getReviewId()).isEqualTo(reviewId);
  }

  @Test
  void shouldSetReviewId() {
    assertThat(relation.getReviewId()).isNull();

    // Usar linkToReview en lugar de setReviewId
    relation.linkToReview(reviewId);

    assertThat(relation.getReviewId()).isEqualTo(reviewId);
  }

  @Test
  void equalsAndHashCodeShouldWorkBasedOnUserIdAndProductId() {
    // Usar reconstruct en lugar del constructor
    ProductUserRelation sameRelation = ProductUserRelation.reconstruct(
        new EntityId(UUID.randomUUID()), productId, userId, new EntityId(UUID.randomUUID()));

    ProductUserRelation differentRelation = ProductUserRelation.reconstruct(
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
    // Usar reconstruct en lugar del constructor
    assertThatThrownBy(() -> ProductUserRelation.reconstruct(null, productId, userId, reviewId))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> ProductUserRelation.reconstruct(id, null, userId, reviewId))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> ProductUserRelation.reconstruct(id, productId, null, reviewId))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldCreateNewRelation() {
    // Test para el método create
    ProductUserRelation newRelation = ProductUserRelation.create(productId, userId);

    assertThat(newRelation.getId()).isNotNull();
    assertThat(newRelation.getProductId()).isEqualTo(productId);
    assertThat(newRelation.getUserId()).isEqualTo(userId);
    assertThat(newRelation.getReviewId()).isNull();
  }

  @Test
  void shouldCheckIfRelationHasReview() {
    // Probar el método hasReview
    assertThat(relation.hasReview()).isFalse();

    relation.linkToReview(reviewId);

    assertThat(relation.hasReview()).isTrue();
  }

  @Test
  void shouldUnlinkReview() {
    relation.linkToReview(reviewId);
    assertThat(relation.getReviewId()).isEqualTo(reviewId);

    relation.unlinkReview();

    assertThat(relation.getReviewId()).isNull();
    assertThat(relation.hasReview()).isFalse();
  }
}