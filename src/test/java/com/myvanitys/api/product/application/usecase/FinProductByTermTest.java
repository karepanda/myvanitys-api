package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinProductByTermTest {

    @Mock
    private JpaProductRepository jpaProductRepository;

    @Mock
    private FindProductService findProductService;

    @InjectMocks
    private FindProductByTerm finProductByTerm;

    @Nested
    class Query {
        @Test
        void when_termProvided_then_returnsProducts() {
            // Arrange
            String searchTerm = "test";
            ProductEntity productEntity1 = new ProductEntity();
            ProductEntity productEntity2 = new ProductEntity();
            List<ProductEntity> productEntities = List.of(productEntity1, productEntity2);

            Product product1 = Product.newProduct("Product 1", "Brand 1", "#FF0000");
            Product product2 = Product.newProduct("Product 2", "Brand 2", "#00FF00");
            List<Product> expectedProducts = List.of(product1, product2);

            when(jpaProductRepository.searchByNameOrBrand(searchTerm))
                    .thenReturn(productEntities);
            when(findProductService.findProducts(productEntities))
                    .thenReturn(expectedProducts);

            // Act
            List<Product> result = finProductByTerm.query(searchTerm);

            // Assert
            assertThat(result).isEqualTo(expectedProducts);
            verify(jpaProductRepository).searchByNameOrBrand(searchTerm);
            verify(findProductService).findProducts(productEntities);
            verifyNoMoreInteractions(jpaProductRepository, findProductService);
        }

        @Test
        void when_noProductsFound_then_returnsEmptyList() {
            // Arrange
            String searchTerm = "nonexistent";
            List<ProductEntity> emptyEntities = List.of();
            List<Product> emptyProducts = List.of();

            when(jpaProductRepository.searchByNameOrBrand(searchTerm))
                    .thenReturn(emptyEntities);
            when(findProductService.findProducts(emptyEntities))
                    .thenReturn(emptyProducts);

            // Act
            List<Product> result = finProductByTerm.query(searchTerm);

            // Assert
            assertThat(result).isEmpty();
            verify(jpaProductRepository).searchByNameOrBrand(searchTerm);
            verify(findProductService).findProducts(emptyEntities);
            verifyNoMoreInteractions(jpaProductRepository, findProductService);
        }
    }
}
