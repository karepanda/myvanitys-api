package com.myvanitys.api.product.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void testCategoryInitialization() {
    EntityId id = new EntityId(UUID.randomUUID());
    String name = "Skincare";
    String code = "SERUM";
    Category category = new Category(id, name, code);

    assertNotNull(category.categoryId());
    assertEquals(name, category.name());
    assertEquals(code, category.code());
  }

  @Test
  void testToString() {
    EntityId id = new EntityId(UUID.randomUUID());
    String name = "Makeup";
    String code = "LIPS";
    Category category = new Category(id, name, code);

    String expected = "Category{" + "id=" + id + ", name='" + name + "', code='" + code + "'}";
    assertEquals(expected, category.toString());
  }
}