package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
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
    private ProductRepository productRepository;

    @InjectMocks
    private FindProductByTerm finProductByTerm;

    @Nested
    class Query {
        @Test
        void when_termProvided_then_returnsProducts() {
            // Arrange
            String searchTerm = "test";
            Product product1 = Product.newProduct("Product 1", "Brand 1", "#FF0000");
            Product product2 = Product.newProduct("Product 2", "Brand 2", "#00FF00");
            List<Product> expectedProducts = List.of(product1, product2);

            when(productRepository.searchProductDetailsByNameOrBrand(searchTerm)).thenReturn(expectedProducts);

            // Act
            List<Product> result = finProductByTerm.query(searchTerm);

            // Assert
            assertThat(result).isEqualTo(expectedProducts);
            verify(productRepository).searchProductDetailsByNameOrBrand(searchTerm);
            verifyNoMoreInteractions(productRepository);
        }

        @Test
        void when_noProductsFound_then_returnsEmptyList() {
            // Arrange
            String searchTerm = "nonexistent";
            List<Product> emptyProducts = List.of();

            when(productRepository.searchProductDetailsByNameOrBrand(searchTerm)).thenReturn(emptyProducts);

            // Act
            List<Product> result = finProductByTerm.query(searchTerm);

            // Assert
            assertThat(result).isEmpty();
            verify(productRepository).searchProductDetailsByNameOrBrand(searchTerm);
            verifyNoMoreInteractions(productRepository);
        }
    }
}
