package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.CategoryCode;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
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
    Category category = new Category(new EntityId(categoryId), categoryName, CategoryCode.FACE);

    // When / Then
    assertThatThrownBy(() -> {
      categoryRepositoryAdapter.save(category);
      flushAndClear();
    })
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasCauseInstanceOf(PropertyValueException.class)
        .satisfies(throwable -> {
          PropertyValueException propertyValueException =
              (PropertyValueException) throwable.getCause();
          assertThat(propertyValueException.getMessage())
              .contains("Detached entity with generated id")
              .contains("uninitialized version value");
        });

    assertThat(jpaCategoryRepository.findById(categoryId)).isEmpty();
  }

  @Test
  @DisplayName("Given the fixed category catalog when deleting a referenced category then the delete is rejected")
  void givenReferencedCategory_whenDeleteById_thenRejectedByForeignKey() {
    // Given
    Category seeded = seededCategory();
    UUID categoryId = seeded.categoryId().getValue();

    // When / Then
    assertThatThrownBy(() -> {
      categoryRepositoryAdapter.deleteById(new EntityId(categoryId));
      flushAndClear();
    }).isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  @DisplayName("Given an invalid category code when persisted directly then the CHECK constraint rejects it")
  void givenInvalidCode_whenPersistedDirectly_thenCheckConstraintRejectsIt() {
    // Given
    CategoryEntity entity = new CategoryEntity();
    entity.setName("Invalid Code Category " + UUID.randomUUID());
    entity.setCode("INVALID");

    // When / Then
    assertThatThrownBy(() -> jpaCategoryRepository.saveAndFlush(entity))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
