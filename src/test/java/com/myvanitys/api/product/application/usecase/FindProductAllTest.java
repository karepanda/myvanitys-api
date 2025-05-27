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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindProductAllTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FindProductAll target;

    @Nested
    class Query {
        @Test
        void when_repositoryReturnsProducts_then_returnsListOfProducts() {
            // Arrange

            final Product product1 = Product.newProduct("Product 1", "Brand 1", "#FF0000");
            final Product product2 = Product.newProduct("Product 2", "Brand 2", "#00FF00");
            final List<Product> expectedProducts = List.of(product1, product2);

            when(productRepository.findAll()).thenReturn(expectedProducts);

            // Act
            final List<Product> result = target.query();

            // Assert
            assertThat(result).isEqualTo(expectedProducts);
        }

        @Test
        void when_repositoryReturnsEmptyList_then_returnsEmptyProductList() {
            // Arrange
            final List<Product> expectedProducts = List.of();

            when(productRepository.findAll()).thenReturn(expectedProducts);

            // Act
            final List<Product> result = target.query();

            // Assert
            assertThat(result).isEmpty();
        }
    }
}