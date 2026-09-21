package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewRepositoryAdapterIT extends AbstractProductPersistenceAdapterIT {

  @Test
  @DisplayName("Given a real product-user relation when a review is saved then the row references that relation and reloads")
  void givenRealRelation_whenReviewSaved_thenPersistsAndReloads() {
    // Given
    ProductUserRelation relation = persistRelationForNewProduct("Review Save Product");
    EntityId reviewId = EntityId.newId();
    Review review = Review.createWithExistingId(reviewId, relation.getId(), ReviewDetails.create(5, "Adapter review"));

    // When
    Review saved = reviewRepositoryAdapter.save(review);
    flushAndClear();

    // Then
    assertThat(saved.getId().getValue()).isEqualTo(reviewId.getValue());
    assertThat(saved.getProductUserId().getValue()).isEqualTo(relation.getId().getValue());
    assertThat(saved.getRating()).isEqualTo(5);
    assertThat(saved.getComment()).isEqualTo("Adapter review");
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getDeletedAt()).isNull();

    assertThat(reviewRepositoryAdapter.findById(reviewId))
        .hasValueSatisfying(reloaded -> {
          assertThat(reloaded.getId().getValue()).isEqualTo(reviewId.getValue());
          assertThat(reloaded.getProductUserId().getValue()).isEqualTo(relation.getId().getValue());
          assertThat(reloaded.getRating()).isEqualTo(5);
          assertThat(reloaded.getComment()).isEqualTo("Adapter review");
          assertThat(reloaded.getCreatedAt()).isNotNull();
          assertThat(reloaded.getUpdatedAt()).isNotNull();
        });

    // Independent raw-row probe of the FK value.
    assertThat(jpaReviewRepository.findById(reviewId.getValue()))
        .hasValueSatisfying(entity -> {
          assertThat(entity.getProductUserId()).isEqualTo(relation.getId().getValue());
          assertThat(entity.getRating()).isEqualTo(5);
          assertThat(entity.getComment()).isEqualTo("Adapter review");
        });
  }

  @Test
  @DisplayName("Given a review whose product-user relation does not exist when saved then it fails and inserts nothing")
  void givenMissingRelation_whenReviewSaved_thenFailsWithoutInsert() {
    // Given
    EntityId reviewId = EntityId.newId();
    Review review = Review.createWithExistingId(
        reviewId, EntityId.newId(), ReviewDetails.create(3, "Orphan review"));

    // When / Then
    assertThatThrownBy(() -> reviewRepositoryAdapter.save(review))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("ProductUser relation not found");

    flushAndClear();
    assertThat(jpaReviewRepository.findById(reviewId.getValue())).isEmpty();
  }

  @Test
  @DisplayName("Given reviews on known relations when looked up by product and user then only the matching reviews are returned")
  void givenReviewsOnKnownRelations_whenLookedUp_thenOnlyMatchingReviewsReturned() {
    // Given
    Category category = seededCategory();
    Product productA = persistProduct(category, uniqueName("Lookup Product A"), "AdapterBrand", "#A1B2C3");
    Product productB = persistProduct(category, uniqueName("Lookup Product B"), "AdapterBrand", "#C3B2A1");

    UUID userOne = UUID.randomUUID();
    UUID userTwo = UUID.randomUUID();
    UUID userThree = UUID.randomUUID();

    ProductUserRelation relationA1 = persistRelation(productA.getId(), new EntityId(userOne));
    ProductUserRelation relationA2 = persistRelation(productA.getId(), new EntityId(userTwo));
    ProductUserRelation relationB3 = persistRelation(productB.getId(), new EntityId(userThree));

    Review reviewA1 = persistReview(relationA1.getId(), 5, "Review A1");
    Review reviewA2 = persistReview(relationA2.getId(), 4, "Review A2");
    Review reviewB3 = persistReview(relationB3.getId(), 3, "Review B3");
    flushAndClear();

    // When / Then: findById
    assertThat(reviewRepositoryAdapter.findById(reviewA1.getId()))
        .hasValueSatisfying(reloaded -> {
          assertThat(reloaded.getProductUserId().getValue()).isEqualTo(relationA1.getId().getValue());
          assertThat(reloaded.getRating()).isEqualTo(5);
          assertThat(reloaded.getComment()).isEqualTo("Review A1");
        });

    // When / Then: findByProductId
    List<Review> reviewsForProductA = reviewRepositoryAdapter.findByProductId(productA.getId());
    assertThat(reviewsForProductA)
        .extracting(found -> found.getId().getValue())
        .contains(reviewA1.getId().getValue(), reviewA2.getId().getValue())
        .doesNotContain(reviewB3.getId().getValue());
    assertThat(reviewsForProductA)
        .allSatisfy(found -> assertThat(found.getProductUserId().getValue())
            .isIn(relationA1.getId().getValue(), relationA2.getId().getValue()));

    // When / Then: findByUserId only returns reviews of that user's relations
    List<Review> reviewsForUserOne = reviewRepositoryAdapter.findByUserId(new EntityId(userOne));
    assertThat(reviewsForUserOne)
        .extracting(found -> found.getId().getValue())
        .containsExactly(reviewA1.getId().getValue());
    assertThat(reviewsForUserOne)
        .noneMatch(found -> found.getId().getValue().equals(reviewA2.getId().getValue())
            || found.getId().getValue().equals(reviewB3.getId().getValue()));
  }

  @Test
  @DisplayName("Given a review when checking ownership then only the owning user and existing review combination is true")
  void givenReview_whenExistsByReviewIdAndUserId_thenOnlyOwnerMatches() {
    // Given
    UUID ownerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    ProductUserRelation relation = persistRelation(persistProductForRelation("Existence Product").getId(), new EntityId(ownerId));
    Review review = persistReview(relation.getId(), 4, "Owner review");
    flushAndClear();

    // When / Then
    assertThat(reviewRepositoryAdapter.existsByReviewIdAndUserId(review.getId(), new EntityId(ownerId))).isTrue();
    assertThat(reviewRepositoryAdapter.existsByReviewIdAndUserId(review.getId(), new EntityId(otherUserId))).isFalse();
    assertThat(reviewRepositoryAdapter.existsByReviewIdAndUserId(EntityId.newId(), new EntityId(ownerId))).isFalse();
  }

  @Test
  @DisplayName("Given a persisted review when deleted through the adapter then the review row is gone")
  void givenPersistedReview_whenDeleteById_thenRowIsRemoved() {
    // Given
    ProductUserRelation relation = persistRelationForNewProduct("Review Delete Product");
    Review review = persistReview(relation.getId(), 2, "Delete me");
    UUID reviewId = review.getId().getValue();
    entityManager.flush();
    assertThat(jpaReviewRepository.findById(reviewId)).isPresent();

    // When
    reviewRepositoryAdapter.deleteById(new EntityId(reviewId));
    flushAndClear();

    // Then
    assertThat(reviewRepositoryAdapter.findById(new EntityId(reviewId))).isEmpty();
    assertThat(jpaReviewRepository.findById(reviewId)).isEmpty();
  }

  private Product persistProductForRelation(String prefix) {
    return persistProduct(seededCategory(), uniqueName(prefix), "AdapterBrand", "#123456");
  }

  private ProductUserRelation persistRelationForNewProduct(String prefix) {
    Product product = persistProductForRelation(prefix);
    return persistRelation(product.getId(), new EntityId(UUID.randomUUID()));
  }

  private String uniqueName(String prefix) {
    return prefix + " " + UUID.randomUUID();
  }
}
