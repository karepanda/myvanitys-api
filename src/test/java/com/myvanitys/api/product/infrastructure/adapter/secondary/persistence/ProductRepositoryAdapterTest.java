package com.myvanitys.api.product.infrastructure.adapter.secondary.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.secondary.ProductRepositoryAdapter;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

  @InjectMocks
  private ProductRepositoryAdapter target;

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private ProductMapper productMapper;

  private EntityId productId;

  private ProductEntity productEntity;

  private Product product;

  private Category category;

  @BeforeEach
  void setUp() {
    // Initialize IDs
    productId = new EntityId(UUID.randomUUID());
    EntityId categoryId = new EntityId(UUID.randomUUID());
    EntityId userId = new EntityId(UUID.randomUUID());

    // Initialize domain objects
    category = new Category(categoryId, "Test Category");
    product = new Product(productId, "Test Product", "Test Brand", category, "#FFFFFF");

    // Initialize entity objects
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(UUID.randomUUID());
    productUserEntity.setProductId(productId.getValue());
    productUserEntity.setUserId(userId.getValue());

    productEntity = new ProductEntity();
    productEntity.setProductId(productId.getValue());
    productEntity.setCategory(new CategoryEntity());
    productEntity.getCategory().setCategoryId(categoryId.getValue());
    productEntity.setName("Test Product");
    productEntity.setBrand("Test Brand");
    productEntity.setColorHex("#FFFFFF");
  }

  @Nested
  class SaveTest {

    @Test
    void save() {
      // Arrange
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(productEntity)).thenReturn(productEntity);
      when(productMapper.toDomain(productEntity)).thenReturn(product);

      // Act
      Product savedProduct = target.save(product);

      // Assert
      assertThat(savedProduct).isEqualTo(product);
      verify(jpaProductRepository).save(productEntity);
    }
  }

  @Nested
  class FindByIdTest {

    @Test
    void findById() {
      // Arrange
      when(jpaProductRepository.findById(productId.getValue())).thenReturn(java.util.Optional.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(product);

      // Act
      Optional<Product> foundProduct = target.findById(productId);

      // Assert
      assertThat(foundProduct).isPresent().get().isEqualTo(product);
      verify(jpaProductRepository).findById(productId.getValue());
    }
  }

  @Nested
  class FindByNameTest {

    @Test
    void findByName() {
      // Arrange
      when(jpaProductRepository.findByName(product.getName())).thenReturn(Optional.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(product);

      // Act
      Optional<Product> foundProduct = target.findByName(product.getName());

      // Assert
      assertThat(foundProduct).isPresent().get().isEqualTo(product);
      verify(jpaProductRepository).findByName(product.getName());
      verify(productMapper).toDomain(productEntity);
    }
  }

  @Nested
  class FindByCategoryNameTest {

    @Test
    void findByCategoryName() {
      // Arrange
      when(jpaProductRepository.findByCategoryName(category.name())).thenReturn(List.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(product);

      // Act
      List<Product> foundProducts = target.findByCategoryName(category.name());

      // Assert
      assertThat(foundProducts).isNotEmpty().containsExactly(product);
      verify(jpaProductRepository).findByCategoryName(category.name());
    }
  }

  @Nested
  class DeleteByIdTest {

    @Test
    void deleteById() {
      // Act
      target.deleteById(productId);

      // Assert
      verify(jpaProductRepository).deleteById(productId.getValue());
    }
  }

  @Nested
  class findByUserIdTest {

    @Test
    void findByUserId() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());

      // Simulate the direct response of jpaProductRepository.findByUserId
      // which is what our current implementation uses
      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));

      when(productMapper.toDomain(productEntity))
          .thenReturn(product);

      // Act
      List<Product> foundProducts = target.findByUserId(userId.getValue());

      // Assert
      assertThat(foundProducts)
          .hasSize(1)
          .containsExactly(product);

      verify(jpaProductRepository).findByUserId(userId.getValue());
      verify(productMapper).toDomain(productEntity);
    }

  }
}