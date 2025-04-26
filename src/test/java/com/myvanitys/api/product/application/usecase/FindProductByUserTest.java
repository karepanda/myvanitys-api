package com.myvanitys.api.product.application.usecase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindProductByUserTest {

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private FindProductByUser target;

  @Nested
  class Query {

    @Test
    void when_userHasProducts_then_returnMappedProducts() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);

      // Creating entities with specific IDs
      final ProductEntity productId1 = createProductEntity("11111111-1111-1111-1111-111111111111");
      final ProductEntity productId2 = createProductEntity("22222222-2222-2222-2222-222222222222");

      // Creating entities with random IDs
      final Category category1 = new Category(new EntityId(UUID.randomUUID()), "Makeup");
      final Category category2 = new Category(new EntityId(UUID.randomUUID()), "Eye Makeup");

      // Creating domain product with specific IDs
      final Product domain1 = new Product(
          new EntityId(UUID.fromString("765d310a-c838-429c-bd82-3172c2d7f49e")),
          "Lipstick", "Maybelline", category1, "#FF0000");
      final Product domain2 = new Product(
          new EntityId(UUID.fromString("cc2217f0-56c7-48f8-beb3-8da8f6f70f23")),
          "Mascara", "L'Oreal", category2, "#000000");

      // Configure repository behavior
      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productId1, productId2));
      when(productMapper.toDomain(productId1)).thenReturn(domain1);
      when(productMapper.toDomain(productId2)).thenReturn(domain2);

      // Act
      final List<Product> result = target.query(query);

      // Assert
      assertThat(result)
          .hasSize(2)
          .containsExactlyInAnyOrder(domain1, domain2); // Cambiado a containsExactlyInAnyOrder
    }

    private ProductEntity createProductEntity(String id) {
      ProductEntity entity = new ProductEntity();
      entity.setProductId(UUID.fromString(id));
      return entity;
    }

    @Test
    void when_userHasNoProducts_then_throwProductNotFoundException() {
      final EntityId userId = new EntityId(UUID.randomUUID());

      final FindProductUserQuery query = new FindProductUserQuery(userId);

      when(jpaProductRepository.findByUserId(userId.getValue())).thenReturn(List.of());

      assertThrows(ProductNotFoundException.class, () -> target.query(query));
    }

    @Test
    void verify_mockInteractions_whenProductsAreFound() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);
      final ProductEntity productEntity = createProductEntity("11111111-1111-1111-1111-111111111111");
      final Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");
      final Product domainProduct = new Product(
          new EntityId(UUID.randomUUID()),
          "Test Product",
          "Test Brand",
          category,
          "#000000"
      );

      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(domainProduct);

      // Act
      target.query(query);

      // Assert
      verify(jpaProductRepository, times(1)).findByUserId(userId.getValue());
      verify(productMapper, times(1)).toDomain(productEntity);
      verifyNoMoreInteractions(jpaProductRepository, productMapper);
    }

    @Test
    void verify_productProperties_whenProductIsFound() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);
      final ProductEntity productEntity = createProductEntity("11111111-1111-1111-1111-111111111111");

      final EntityId categoryId = new EntityId(UUID.randomUUID());
      final Category category = new Category(categoryId, "Test Category");

      final Product expectedProduct = new Product(
          new EntityId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
          "Test Product",
          "Test Brand",
          category,
          "#FF0000"
      );

      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(expectedProduct);

      // Act
      List<Product> result = target.query(query);

      // Assert
      assertThat(result).hasSize(1);
      Product actualProduct = result.getFirst();
      assertAll(
          () -> assertEquals(expectedProduct.getName(), actualProduct.getName()),
          () -> assertEquals(expectedProduct.getBrand(), actualProduct.getBrand()),
          () -> assertEquals(expectedProduct.getCategory(), actualProduct.getCategory()),
          () -> assertEquals(expectedProduct.getColorHex(), actualProduct.getColorHex())
      );
    }

    @Test
    void verify_exceptionIsThrown_whenProductMapperReturnsNull() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);
      final ProductEntity productEntity = createProductEntity("11111111-1111-1111-1111-111111111111");

      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(null);

      // Act & Assert
      assertThrows(ProductNotFoundException.class, () -> target.query(query));
    }

    @Test
    void verify_emptyList_whenRepositoryReturnsNull() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);

      when(jpaProductRepository.findByUserId(userId.getValue())).thenReturn(null);

      // Act & Assert
      assertThrows(ProductNotFoundException.class, () -> target.query(query));
    }
  }
}