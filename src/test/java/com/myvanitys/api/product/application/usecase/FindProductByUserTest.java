package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindProductByUserTest {

    @Mock
    private JpaProductRepository jpaProductRepository;

    @Mock
    private FindProductService findProductService;

    @InjectMocks
    private FindProductByUser target;

    @Nested
    class Query {
        @Test
        void when_givenValidQuery_then_returnsListOfProducts() {
            // Arrange
            final UUID userIdValue = UUID.fromString("11111111-1111-1111-1111-111111111111");
            final EntityId userId = new EntityId(userIdValue);
            final FindProductUserQuery query = new FindProductUserQuery(userId);

            final ProductEntity productEntity1 = new ProductEntity();
            final ProductEntity productEntity2 = new ProductEntity();
            final List<ProductEntity> productEntities = List.of(productEntity1, productEntity2);

            final Product product1 = Product.newProduct("Product 1", "Brand 1", "#FF0000");
            final Product product2 = Product.newProduct("Product 2", "Brand 2", "#00FF00");
            final List<Product> expectedProducts = List.of(product1, product2);

            when(jpaProductRepository.findByUserId(userIdValue)).thenReturn(productEntities);
            when(findProductService.findProducts(productEntities)).thenReturn(expectedProducts);

            // Act
            final List<Product> result = target.query(query);

            // Assert
            assertThat(result).isEqualTo(expectedProducts);
        }

        @Test
        void when_givenQueryReturnsEmptyList_then_returnsEmptyProductList() {
            // Arrange
            final UUID userIdValue = UUID.fromString("22222222-2222-2222-2222-222222222222");
            final EntityId userId = new EntityId(userIdValue);
            final FindProductUserQuery query = new FindProductUserQuery(userId);
            final List<ProductEntity> productEntities = List.of();
            final List<Product> expectedProducts = List.of();

            when(jpaProductRepository.findByUserId(userIdValue)).thenReturn(productEntities);
            when(findProductService.findProducts(productEntities)).thenReturn(expectedProducts);

            // Act
            final List<Product> result = target.query(query);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}