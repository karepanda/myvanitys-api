package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductRepositoryAdapterIT extends AbstractProductPersistenceAdapterIT {

  @Test
  @DisplayName("Given a persisted category when a basic product is saved then it reloads with its category and raw timestamps")
  void givenPersistedCategory_whenBasicProductSaved_thenReloadsWithCategoryAndTimestamps() {
    // Given
    Category category = seededCategory();
    UUID productId = UUID.randomUUID();
    String productName = unique("Basic Product");
    Product product = Product.reconstruct(
        new EntityId(productId), productName, "AdapterBrand", category, "#FF00AA", List.of(), Set.of());

    // When
    Product saved = productRepositoryAdapter.save(product);
    flushAndClear();

    // Then
    assertThat(saved.getId().getValue()).isEqualTo(productId);

    Optional<Product> reloaded = productRepositoryAdapter.findById(new EntityId(productId));
    assertThat(reloaded).isPresent();
    reloaded.ifPresent(found -> {
      assertThat(found.getId().getValue()).isEqualTo(productId);
      assertThat(found.getName()).isEqualTo(productName);
      assertThat(found.getBrand()).isEqualTo("AdapterBrand");
      assertThat(found.getColorHex()).isEqualTo("#FF00AA");
      assertThat(found.getCategory().categoryId().getValue()).isEqualTo(category.categoryId().getValue());
      assertThat(found.getCategory().name()).isEqualTo(category.name());
      assertThat(found.getReviews()).isEmpty();
      assertThat(found.getUserRelations()).isEmpty();
    });

    assertThat(jpaProductRepository.findById(productId))
        .hasValueSatisfying(entity -> {
          assertThat(entity.getCategoryId()).isEqualTo(category.categoryId().getValue());
          assertThat(entity.getCreatedAt()).isNotNull();
          assertThat(entity.getUpdatedAt()).isNotNull();
        });
  }

  @Test
  @DisplayName("Given a product with a non-persisted category when saved then it is rejected and no product row is inserted")
  void givenMissingCategory_whenProductSaved_thenRejectedWithoutInsert() {
    // Given
    UUID missingCategoryId = UUID.randomUUID();
    Category missingCategory = new Category(new EntityId(missingCategoryId), "Missing Category " + missingCategoryId);
    Product product = Product.reconstruct(
        EntityId.newId(), unique("Missing Category Product"), "AdapterBrand", missingCategory, "#000000",
        List.of(), Set.of());

    // When / Then
    assertThatThrownBy(() -> productRepositoryAdapter.save(product))
        .isInstanceOf(RepositoryResourceNotFoundException.class)
        .hasMessageContaining(missingCategoryId.toString());

    assertThat(jpaProductRepository.findById(product.getId().getValue())).isEmpty();
  }

  @Test
  @DisplayName("Given persisted category and relation when the aggregate is saved then relation and review are orchestrated without duplicates")
  void givenPersistedCategoryAndRelation_whenAggregateSaved_thenOrchestratesRelationAndReview() {
    // Given
    Category category = seededCategory();
    Product baseProduct = persistProduct(category, unique("Orchestration Product"), "AdapterBrand", "#00AAFF");
    UUID productId = baseProduct.getId().getValue();
    UUID userId = UUID.randomUUID();
    ProductUserRelation relation = persistRelation(baseProduct.getId(), new EntityId(userId));
    Review review = Review.createWithExistingId(
        EntityId.newId(), relation.getId(), ReviewDetails.create(5, "Orchestrated review"));

    Product aggregate = Product.reconstruct(
        new EntityId(productId),
        baseProduct.getName(),
        baseProduct.getBrand(),
        category,
        baseProduct.getColorHex(),
        List.of(review),
        Set.of(relation));

    // When
    productRepositoryAdapter.save(aggregate);
    flushAndClear();

    // Then
    Optional<Product> reloaded = productRepositoryAdapter.findById(new EntityId(productId));
    assertThat(reloaded).isPresent();
    reloaded.ifPresent(found -> {
      assertThat(found.getId().getValue()).isEqualTo(productId);
      assertThat(found.getCategory().categoryId().getValue()).isEqualTo(category.categoryId().getValue());

      assertThat(found.getUserRelations())
          .singleElement()
          .satisfies(foundRelation -> {
            assertThat(foundRelation.getProductId().getValue()).isEqualTo(productId);
            assertThat(foundRelation.getUserId().getValue()).isEqualTo(userId);
          });

      assertThat(found.getReviews())
          .singleElement()
          .satisfies(foundReview -> {
            assertThat(foundReview.getId().getValue()).isEqualTo(review.getId().getValue());
            assertThat(foundReview.getProductUserId().getValue()).isEqualTo(relation.getId().getValue());
            assertThat(foundReview.getRating()).isEqualTo(5);
            assertThat(foundReview.getComment()).isEqualTo("Orchestrated review");
          });
    });

    // Raw rows, including FK values and duplicate detection for the relation.
    assertThat(jpaProductRepository.findById(productId)).isPresent();
    List<ProductUserEntity> relationRows = jpaProductUserRepository.findByProductId(productId);
    assertThat(relationRows).singleElement().satisfies(row -> {
      assertThat(row.getUserId()).isEqualTo(userId);
      assertThat(row.getProductId()).isEqualTo(productId);
    });
    assertThat(jpaReviewRepository.findById(review.getId().getValue()))
        .hasValueSatisfying(row -> {
          assertThat(row.getProductUserId()).isEqualTo(relation.getId().getValue());
          assertThat(row.getRating()).isEqualTo(5);
          assertThat(row.getComment()).isEqualTo("Orchestrated review");
        });
  }

  @Test
  @DisplayName("Given adapter-created data when looked up by user and search then only the matching product and reviews return")
  void givenAdapterCreatedData_whenUserAndSearchLookups_thenOnlyMatchingDataReturned() {
    // Given
    Category category = seededCategory();
    String token = "adapterit" + UUID.randomUUID().toString().replace("-", "");
    Product target = persistProduct(category, "Lookup " + token, "Brand " + token, "#0F0F0F");
    UUID targetId = target.getId().getValue();
    UUID targetUser = UUID.randomUUID();
    ProductUserRelation relation = persistRelation(target.getId(), new EntityId(targetUser));
    Review review = persistReview(relation.getId(), 5, "Search review " + token);

    Product unrelated = persistProduct(category, unique("Unrelated Product"), "AdapterBrand", "#F0F0F0");
    UUID unrelatedId = unrelated.getId().getValue();
    persistRelation(unrelated.getId(), new EntityId(UUID.randomUUID()));
    flushAndClear();

    // When / Then: findByUserId
    List<Product> productsForUser = productRepositoryAdapter.findByUserId(targetUser);
    assertThat(productsForUser)
        .extracting(found -> found.getId().getValue())
        .contains(targetId)
        .doesNotContain(unrelatedId);
    assertThat(productsForUser)
        .filteredOn(found -> found.getId().getValue().equals(targetId))
        .singleElement()
        .satisfies(found -> assertThat(found.getReviews())
            .extracting(foundReview -> foundReview.getProductUserId().getValue())
            .containsOnly(relation.getId().getValue()));

    // When / Then: search by a unique term avoids any Flyway seed match
    List<Product> searchResults = productRepositoryAdapter.searchProductDetailsByNameOrBrand(token);
    assertThat(searchResults).hasSize(1);
    Product searchHit = searchResults.getFirst();
    assertThat(searchHit.getId().getValue()).isEqualTo(targetId);
    assertThat(searchHit.getCategory().categoryId().getValue()).isEqualTo(category.categoryId().getValue());
    assertThat(searchHit.getCategory().name()).isEqualTo(category.name());
    assertThat(searchHit.getReviews())
        .singleElement()
        .satisfies(foundReview -> {
          assertThat(foundReview.getId().getValue()).isEqualTo(review.getId().getValue());
          assertThat(foundReview.getProductUserId().getValue()).isEqualTo(relation.getId().getValue());
          assertThat(foundReview.getRating()).isEqualTo(5);
          assertThat(foundReview.getComment()).isEqualTo("Search review " + token);
        });
    assertThat(searchResults).noneMatch(hit -> hit.getId().getValue().equals(unrelatedId));
  }

  @Test
  @DisplayName("Given a product without dependent relations when deleted through the adapter then the product row is gone")
  void givenProductWithoutRelations_whenDeleteById_thenRowIsRemoved() {
    // Given
    Product product = persistProduct(seededCategory(), unique("Delete Product"), "AdapterBrand", "#ABCDEF");
    UUID productId = product.getId().getValue();
    assertThat(jpaProductRepository.findById(productId)).isPresent();

    // When
    productRepositoryAdapter.deleteById(new EntityId(productId));
    flushAndClear();

    // Then
    assertThat(productRepositoryAdapter.findById(new EntityId(productId))).isEmpty();
    assertThat(jpaProductRepository.findById(productId)).isEmpty();
  }

  private String unique(String prefix) {
    return prefix + " " + UUID.randomUUID();
  }
}
