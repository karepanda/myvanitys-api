package com.myvanitys.api.product.domain;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {
    @Test
    void testCategoryInitialization() {
        EntityId id = new EntityId(UUID.randomUUID());
        String name = "Skincare";
        Category category = new Category(id, name);

        assertNotNull(category.getId());
        assertEquals(name, category.getName());
    }

    @Test
    void testNonNullId() {
        Exception exception = assertThrows(NullPointerException.class, () -> new Category(null, "Skincare"));
        assertEquals("id is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testNonNullName() {
        EntityId id = new EntityId(UUID.randomUUID());
        Exception exception = assertThrows(NullPointerException.class, () -> new Category(id, null));
        assertEquals("name is marked non-null but is null", exception.getMessage());
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