package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductUserRepositoryAdapterIT extends AbstractProductPersistenceAdapterIT {

  @Test
  @DisplayName("Given a persisted product and user when a relation is saved then every lookup path returns it")
  void givenPersistedProductAndUser_whenRelationSaved_thenAllLookupsReturnIt() {
    // Given
    Category category = seededCategory();
    Product product = persistProduct(category, uniqueName("Relation Product"), "AdapterBrand", "#AABBCC");
    UUID productId = product.getId().getValue();
    UUID userId = UUID.randomUUID();
    EntityId productEntityId = new EntityId(productId);
    EntityId userEntityId = new EntityId(userId);

    // When
    productUserRepositoryAdapter.saveProductUserRelationship(productEntityId, userEntityId);
    flushAndClear();

    // Then
    assertThat(productUserRepositoryAdapter.existsByProductIdAndUserId(productEntityId, userEntityId)).isTrue();
    assertThat(productUserRepositoryAdapter.isUserAssociatedWithProduct(productEntityId, userEntityId)).isTrue();
    assertThat(productUserRepositoryAdapter.existsByProductIdAndUserId(productEntityId, new EntityId(UUID.randomUUID())))
        .isFalse();

    assertThat(productUserRepositoryAdapter.findByProductIdAndUserId(productId, userId))
        .hasValueSatisfying(relation -> {
          assertThat(relation.getId().getValue()).isNotNull();
          assertThat(relation.getProductId().getValue()).isEqualTo(productId);
          assertThat(relation.getUserId().getValue()).isEqualTo(userId);
        });

    assertThat(productUserRepositoryAdapter.findByProductId(productId))
        .anySatisfy(relation -> {
          assertThat(relation.getProductId().getValue()).isEqualTo(productId);
          assertThat(relation.getUserId().getValue()).isEqualTo(userId);
        });

    assertThat(productUserRepositoryAdapter.findByUserId(userId))
        .anySatisfy(relation -> {
          assertThat(relation.getProductId().getValue()).isEqualTo(productId);
          assertThat(relation.getUserId().getValue()).isEqualTo(userId);
        });

    assertThat(productUserRepositoryAdapter.findProductIdsByUserId(userEntityId))
        .anySatisfy(id -> assertThat(id.getValue()).isEqualTo(productId));

    // Independent raw-row probe.
    assertThat(jpaProductUserRepository.findByProductIdAndUserId(productId, userId))
        .hasValueSatisfying(entity -> {
          assertThat(entity.getProductId()).isEqualTo(productId);
          assertThat(entity.getUserId()).isEqualTo(userId);
        });
  }

  @Test
  @DisplayName("Given an existing relation when saved again then the unique constraint is respected and one row remains")
  void givenExistingRelation_whenSavedAgain_thenOnlyOneRowRemains() {
    // Given
    Product product = persistProduct(seededCategory(), uniqueName("Idempotent Product"), "AdapterBrand", "#112233");
    UUID productId = product.getId().getValue();
    UUID userId = UUID.randomUUID();
    EntityId productEntityId = new EntityId(productId);
    EntityId userEntityId = new EntityId(userId);

    // When
    productUserRepositoryAdapter.saveProductUserRelationship(productEntityId, userEntityId);
    productUserRepositoryAdapter.saveProductUserRelationship(productEntityId, userEntityId);
    flushAndClear();

    // Then
    List<ProductUserEntity> matchingRows = jpaProductUserRepository.findByProductId(productId).stream()
        .filter(entity -> entity.getUserId().equals(userId))
        .toList();
    assertThat(matchingRows).hasSize(1);
    assertThat(jpaProductUserRepository.findByProductIdAndUserId(productId, userId)).isPresent();
  }

  @Test
  @DisplayName("Given two relations when one is deleted then only the unrelated relation survives")
  void givenTwoRelations_whenDeleteOnePair_thenOnlyTheOtherSurvives() {
    // Given
    Product product = persistProduct(seededCategory(), uniqueName("Delete One Product"), "AdapterBrand", "#445566");
    UUID productId = product.getId().getValue();
    UUID deletedUserId = UUID.randomUUID();
    UUID remainingUserId = UUID.randomUUID();
    productUserRepositoryAdapter.saveProductUserRelationship(new EntityId(productId), new EntityId(deletedUserId));
    productUserRepositoryAdapter.saveProductUserRelationship(new EntityId(productId), new EntityId(remainingUserId));
    flushAndClear();

    // When
    productUserRepositoryAdapter.deleteByProductIdAndUserId(productId, deletedUserId);
    flushAndClear();

    // Then
    assertThat(productUserRepositoryAdapter.findByProductIdAndUserId(productId, deletedUserId)).isEmpty();
    assertThat(productUserRepositoryAdapter.existsByProductIdAndUserId(new EntityId(productId), new EntityId(deletedUserId)))
        .isFalse();
    assertThat(productUserRepositoryAdapter.findByProductIdAndUserId(productId, remainingUserId)).isPresent();
    assertThat(productUserRepositoryAdapter.existsByProductIdAndUserId(new EntityId(productId), new EntityId(remainingUserId)))
        .isTrue();
  }

  @Test
  @DisplayName("Given relations on two products when all relations of one product are deleted then the other product is untouched")
  void givenRelationsOnTwoProducts_whenDeleteByProductId_thenOnlyThatProductIsCleared() {
    // Given
    Category category = seededCategory();
    Product firstProduct = persistProduct(category, uniqueName("Delete All First"), "AdapterBrand", "#778899");
    Product secondProduct = persistProduct(category, uniqueName("Delete All Second"), "AdapterBrand", "#99AABB");
    UUID firstProductId = firstProduct.getId().getValue();
    UUID secondProductId = secondProduct.getId().getValue();
    UUID secondUserId = UUID.randomUUID();

    productUserRepositoryAdapter.saveProductUserRelationship(new EntityId(firstProductId), new EntityId(UUID.randomUUID()));
    productUserRepositoryAdapter.saveProductUserRelationship(new EntityId(firstProductId), new EntityId(UUID.randomUUID()));
    productUserRepositoryAdapter.saveProductUserRelationship(new EntityId(secondProductId), new EntityId(secondUserId));
    flushAndClear();

    // When
    productUserRepositoryAdapter.deleteByProductId(new EntityId(firstProductId));
    flushAndClear();

    // Then
    assertThat(productUserRepositoryAdapter.findByProductId(firstProductId)).isEmpty();
    assertThat(jpaProductUserRepository.findByProductId(firstProductId)).isEmpty();

    assertThat(productUserRepositoryAdapter.findByProductId(secondProductId))
        .singleElement()
        .satisfies(relation -> {
          assertThat(relation.getProductId().getValue()).isEqualTo(secondProductId);
          assertThat(relation.getUserId().getValue()).isEqualTo(secondUserId);
        });
  }

  @Test
  @DisplayName("Given a relation with a review when the relation is deleted then the review is cascaded away by the database")
  void givenRelationWithReview_whenRelationDeleted_thenReviewIsCascadeDeleted() {
    // Given
    Product product = persistProduct(seededCategory(), uniqueName("Cascade Product"), "AdapterBrand", "#C0FFEE");
    UUID productId = product.getId().getValue();
    UUID userId = UUID.randomUUID();
    ProductUserRelation relation = persistRelation(new EntityId(productId), new EntityId(userId));
    Review review = persistReview(relation.getId(), 4, "Cascade me");
    UUID reviewId = review.getId().getValue();
    entityManager.flush();
    assertThat(jpaReviewRepository.findById(reviewId)).isPresent();

    // When
    productUserRepositoryAdapter.deleteByProductIdAndUserId(productId, userId);
    flushAndClear();

    // Then
    assertThat(productUserRepositoryAdapter.findByProductIdAndUserId(productId, userId)).isEmpty();
    assertThat(jpaReviewRepository.findById(reviewId)).isEmpty();
  }

  private String uniqueName(String prefix) {
    return prefix + " " + UUID.randomUUID();
  }
}
