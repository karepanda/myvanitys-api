package com.myvanitys.api.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void testCategoryInitialization() {
    EntityId id = new EntityId(UUID.randomUUID());
    String name = "Skincare";
    Category category = new Category(id, name);

    assertNotNull(category.categoryId());
    assertEquals(name, category.name());
  }

  @Test
  void testToString() {
    EntityId id = new EntityId(UUID.randomUUID());
    String name = "Makeup";
    Category category = new Category(id, name);

    String expected = "Category{" + "id=" + id + ", name='" + name + "'}";
    assertEquals(expected, category.toString());
  }
}