package com.myvanitys.api.product.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandToProductMapperTest {

  private CommandToProductMapper commandToProductMapper;

  private CreateProductCommand createProductCommand;

  private Category category;

  @BeforeEach
  void setUp() {
    commandToProductMapper = Mappers.getMapper(CommandToProductMapper.class);

    EntityId categoryId = new EntityId(UUID.randomUUID());
    EntityId userId = new EntityId(UUID.randomUUID());

    createProductCommand = new CreateProductCommand(
        "Serum",
        "BrandY",
        categoryId,
        "#FF5733",
        userId
    );

    category = new Category(categoryId, "Skincare");
  }

  @Test
  void shouldMapCreateProductCommandToProduct() {
    // When
    Product product = commandToProductMapper.toProduct(createProductCommand, category);

    // Then
    assertNotNull(product);
    assertEquals(createProductCommand.name(), product.getName());
    assertEquals(createProductCommand.brand(), product.getBrand());
    assertEquals(createProductCommand.colorHex(), product.getColorHex());

    // Validamos categoría
    assertNotNull(product.getCategory());
    assertEquals(category.categoryId(), product.getCategory().categoryId());
    assertEquals(category.name(), product.getCategory().name());
  }
}