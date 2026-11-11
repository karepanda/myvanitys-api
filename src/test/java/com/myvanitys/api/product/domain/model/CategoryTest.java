package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.exception.CategoryValidationException;
import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void testCategoryInitialization() {
    EntityId id = new EntityId(UUID.randomUUID());
    String name = "Skincare";
    CategoryCode code = CategoryCode.SERUM;
    Category category = new Category(id, name, code);

    assertNotNull(category.categoryId());
    assertEquals(name, category.name());
    assertEquals(code, category.code());
  }

  @Test
  void testToString() {
    EntityId id = new EntityId(UUID.randomUUID());
    String name = "Makeup";
    CategoryCode code = CategoryCode.LIPS;
    Category category = new Category(id, name, code);

    String expected = "Category{" + "id=" + id + ", name='" + name + "', code='" + code + "'}";
    assertEquals(expected, category.toString());
  }

  @Test
  void rejectsNullCode() {
    EntityId id = new EntityId(UUID.randomUUID());

    assertThatThrownBy(() -> new Category(id, "Skincare", null))
        .isInstanceOf(CategoryValidationException.class)
        .hasMessage("Category code cannot be null");
  }
}
