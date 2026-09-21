package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class CategoryRepositoryAdapterIT extends AbstractProductPersistenceAdapterIT {

  private static final UUID SEEDED_FACE_CATEGORY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  @DisplayName("Given a Flyway-seeded category when read through the adapter then id and name are mapped independently")
  void givenSeededCategory_whenReadThroughAdapter_thenMapsIdAndName() {
    // When
    Optional<Category> byId = categoryRepositoryAdapter.findById(new EntityId(SEEDED_FACE_CATEGORY_ID));
    Optional<Category> byName = categoryRepositoryAdapter.findByName(SEEDED_CATEGORY_NAME);

    // Then
    assertThat(byId).isPresent();
    byId.ifPresent(found -> {
      assertThat(found.categoryId().getValue()).isEqualTo(SEEDED_FACE_CATEGORY_ID);
      assertThat(found.name()).isEqualTo(SEEDED_CATEGORY_NAME);
    });

    assertThat(byName).isPresent();
    byName.ifPresent(found -> {
      assertThat(found.categoryId().getValue()).isEqualTo(SEEDED_FACE_CATEGORY_ID);
      assertThat(found.name()).isEqualTo(SEEDED_CATEGORY_NAME);
    });

    List<Category> all = categoryRepositoryAdapter.findAll();
    assertThat(all)
        .filteredOn(candidate -> candidate.categoryId().getValue().equals(SEEDED_FACE_CATEGORY_ID))
        .singleElement()
        .satisfies(found -> assertThat(found.name()).isEqualTo(SEEDED_CATEGORY_NAME));
  }

  @Test
  @DisplayName("Known defect: saving a new category through the adapter fails for its assigned generated id")
  void givenNewCategory_whenSavedThroughAdapter_thenFailsBecauseGeneratedIdIsAlreadyAssigned() {
    // Given
    UUID categoryId = UUID.randomUUID();
    String categoryName = "Adapter IT Category " + categoryId;
    Category category = new Category(new EntityId(categoryId), categoryName);

    // When / Then
    assertThatThrownBy(() -> {
      categoryRepositoryAdapter.save(category);
      flushAndClear();
    }).isInstanceOf(DataIntegrityViolationException.class);

    assertThat(jpaCategoryRepository.findById(categoryId)).isEmpty();
  }

  @Test
  @DisplayName("Given a persisted category without products when deleted through the adapter then the row is gone")
  void givenPersistedCategoryWithoutProducts_whenDeleteById_thenRowIsRemoved() {
    // Given
    CategoryEntity saved = insertCategoryDirectly("Adapter IT Deletable " + UUID.randomUUID());
    UUID categoryId = saved.getCategoryId();
    flushAndClear();
    assertThat(jpaCategoryRepository.findById(categoryId)).isPresent();

    // When
    categoryRepositoryAdapter.deleteById(new EntityId(categoryId));
    flushAndClear();

    // Then
    assertThat(categoryRepositoryAdapter.findById(new EntityId(categoryId))).isEmpty();
    assertThat(jpaCategoryRepository.findById(categoryId)).isEmpty();
  }
}
