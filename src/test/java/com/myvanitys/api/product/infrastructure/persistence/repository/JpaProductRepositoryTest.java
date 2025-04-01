package com.myvanitys.api.product.infrastructure.persistence.repository;

import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JpaProductRepositoryTest {
    @Mock
    private JpaProductRepository jpaProductRepository;

    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        // Initialize IDs
        CategoryEntity categoryEntity = new CategoryEntity();
        productEntity = new ProductEntity();
        productEntity.setProductId(UUID.randomUUID());
        productEntity.setCategory(categoryEntity);
        productEntity.setName("Moisturizer");
        productEntity.setBrand("BrandX");
        categoryEntity.setName("Skincare");
    }

    @Test
    void shouldSaveAndRetrieveProduct() {
        // Arrange
        when(jpaProductRepository.save(productEntity)).thenReturn(productEntity);
        when(jpaProductRepository.findById(productEntity.getProductId())).thenReturn(Optional.of(productEntity));

        // Act
        ProductEntity savedProduct = jpaProductRepository.save(productEntity);
        Optional<ProductEntity> retrievedProduct = jpaProductRepository.findById(savedProduct.getProductId());

        // Assert
        assertThat(retrievedProduct).isPresent();
        assertThat(retrievedProduct.get().getName()).isEqualTo("Moisturizer");
        verify(jpaProductRepository, times(1)).save(productEntity);
        verify(jpaProductRepository, times(1)).findById(productEntity.getProductId());
    }

    @Test
    void shouldUpdateProduct() {
        // Arrange
        productEntity.setName("Updated Moisturizer");
        when(jpaProductRepository.save(productEntity)).thenReturn(productEntity);
        when(jpaProductRepository.findById(productEntity.getProductId())).thenReturn(Optional.of(productEntity));

        // Act
        ProductEntity updatedProduct = jpaProductRepository.save(productEntity);
        Optional<ProductEntity> retrievedProduct = jpaProductRepository.findById(updatedProduct.getProductId());

        // Assert
        assertThat(retrievedProduct).isPresent();
        assertThat(retrievedProduct.get().getName()).isEqualTo("Updated Moisturizer");
        verify(jpaProductRepository, times(1)).save(productEntity);
        verify(jpaProductRepository, times(1)).findById(productEntity.getProductId());
    }

    @Test
    void shouldFindProductByName() {
        // Arrange
        when(jpaProductRepository.findByName("Moisturizer")).thenReturn(Optional.of(productEntity));

        // Act
        Optional<ProductEntity> foundProduct = jpaProductRepository.findByName("Moisturizer");

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Moisturizer");
        verify(jpaProductRepository, times(1)).findByName("Moisturizer");
    }

    @Test
    void shouldFindProductByBrand() {
        // Arrange
        when(jpaProductRepository.findByBrand("BrandX")).thenReturn(Optional.of(productEntity));

        // Act
        Optional<ProductEntity> foundProduct = jpaProductRepository.findByBrand("BrandX");

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getBrand()).isEqualTo("BrandX");
        verify(jpaProductRepository, times(1)).findByBrand("BrandX");
    }

    @Test
    void shouldFindProductByCategory() {
        // Arrange
        UUID categoryId = productEntity.getCategory().getCategoryId();
        when(jpaProductRepository.findByCategoryCategoryId(categoryId)).thenReturn(List.of(productEntity));

        // Act
        List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryCategoryId(categoryId);

        // Assert
        assertThat(foundProducts.getFirst().getCategory().getCategoryId()).isEqualTo(categoryId);
        verify(jpaProductRepository, times(1)).findByCategoryCategoryId(categoryId);
    }

    @Test
    void shouldFindProductByCategoryName() {
        // Arrange
        String categoryName = "Skincare";
        when(jpaProductRepository.findByCategoryName(categoryName)).thenReturn(List.of(productEntity));

        // Act
        List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryName(categoryName);

        // Assert
        assertThat(foundProducts.getFirst().getCategory().getName()).isEqualTo(categoryName);
        verify(jpaProductRepository, times(1)).findByCategoryName(categoryName);
    }

    @Test
    void shouldFindProductByNameAndBrand() {
        // Arrange
        when(jpaProductRepository.findByNameAndBrand("Moisturizer", "BrandX")).thenReturn(Optional.of(productEntity));

        // Act
        Optional<ProductEntity> foundProduct = jpaProductRepository.findByNameAndBrand("Moisturizer", "BrandX");

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Moisturizer");
        assertThat(foundProduct.get().getBrand()).isEqualTo("BrandX");
        verify(jpaProductRepository, times(1)).findByNameAndBrand("Moisturizer", "BrandX");
    }

    @Test
    void shouldDeleteProductById() {
        // Arrange
        UUID productId = productEntity.getProductId();
        doNothing().when(jpaProductRepository).deleteById(productId);
        when(jpaProductRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        jpaProductRepository.deleteById(productId);
        Optional<ProductEntity> deletedProduct = jpaProductRepository.findById(productId);

        // Assert
        assertThat(deletedProduct).isEmpty();
        verify(jpaProductRepository, times(1)).deleteById(productId);
    }

    @Test
    void shouldFindProductsByUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(jpaProductRepository.findByUserId(userId)).thenReturn(List.of(productEntity));

        // Act
        List<ProductEntity> foundProducts = jpaProductRepository.findByUserId(userId);

        // Assert
        assertThat(foundProducts.get(0).getName()).isEqualTo("Moisturizer");
        verify(jpaProductRepository, times(1)).findByUserId(userId);
    }

}