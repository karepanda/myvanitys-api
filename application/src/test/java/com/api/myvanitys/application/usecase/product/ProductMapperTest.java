package com.api.myvanitys.application.usecase.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.myvanitys.domain.model.Category;
import com.api.myvanitys.domain.model.Product;
import com.api.myvanitys.domain.valueobject.EntityId;
import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.CategoryEntity;
import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Test;

public class ProductMapperTest {

  private final ProductMapper productMapper = ProductMapper.INSTANCE;

  private final LocalDateTime actualDate = LocalDateTime.now();

  @Test
  public void testToDomain() {
    ProductEntity entity = new ProductEntity();
    entity.setId(UUID.randomUUID());
    entity.setColorHex(123456);
    entity.setBrand("Chillab");
    entity.setName("Lipstick");
    entity.setCategoryEntity(new CategoryEntity(UUID.randomUUID(), ""));
    // Rellena otros campos de la entidad

    Product product = ProductMapper.INSTANCE.toDomain(entity);

    assertNotNull(product);
    assertEquals(entity.getId(), product.getId().value());
    // Verifica otras propiedades
  }

  @Test
  public void testToEntity() {
    Product product = new Product(
        new EntityId(UUID.randomUUID()),
        "Lipstick",
        "Chillab",
        new Category(new EntityId(UUID.randomUUID()), "Lip Products"),
        "#FF5733"
    );
    // Rellena otros campos del producto

    ProductEntity entity = ProductMapper.INSTANCE.toEntity(product);

    assertNotNull(entity);
    assertEquals(product.getId().value(), entity.getId());
    // Verifica otras propiedades
  }

  @Test
  void shouldMapProductEntityToProduct() {
    // GIVEN: Un ProductEntity con valores simulados
    ProductEntity entity = new ProductEntity();
    entity.setId(UUID.randomUUID());
    entity.setName("Lipstick");
    entity.setBrand("Chillab");
    entity.setColorHex(0xFF5733);

    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setId(UUID.randomUUID());
    categoryEntity.setName("Lip Products");
    entity.setCategoryEntity(categoryEntity);

    // WHEN: Se hace la conversión con el mapper
    Product product = productMapper.toDomain(entity);

    // THEN: Se validan los valores convertidos correctamente
    assertNotNull(product);
    assertEquals(entity.getId(), product.getId().value()); // UUID directo
    assertEquals(entity.getName(), product.getName());
    assertEquals(entity.getBrand(), product.getBrand());
    assertEquals("#FF5733", product.getColorHex());
    assertEquals(categoryEntity.getName(), product.getCategory().getName());
  }

  @Test
  void shouldMapProductToProductEntity() {
    // GIVEN: Un Product con valores simulados
    Product product = new Product(
        new EntityId(UUID.randomUUID()),  // Pasar UUID en lugar de String
        "Lipstick",
        "Chillab",
        new Category(new EntityId(UUID.randomUUID()), "Lip Products"),
        "#FF5733"
    );

    // WHEN: Se convierte a ProductEntity
    ProductEntity entity = productMapper.toEntity(product);

    // THEN: Se validan los valores convertidos correctamente
    assertNotNull(entity);
    assertEquals(product.getId().value(), entity.getId()); // UUID directo
    assertEquals(product.getName(), entity.getName());
    assertEquals(product.getBrand(), entity.getBrand());
    assertEquals(0xFF5733, entity.getColorHex());
    assertEquals(product.getCategory().getName(), entity.getCategoryEntity().getName());
  }

  @Test
  void shouldHandleNullColorHex() {
    // GIVEN: Un ProductEntity sin colorHex
    ProductEntity entity = new ProductEntity();
    entity.setName("Lipstick");
    entity.setBrand("Chillab");

    // WHEN: Se convierte a Product
    Product product = productMapper.toDomain(entity);

    // THEN: Se valida que colorHex sea null
    assertNull(product.getColorHex());
  }
}
