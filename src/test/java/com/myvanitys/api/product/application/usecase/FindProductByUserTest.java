package com.myvanitys.api.product.application.usecase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

            // Crear entidades con IDs específicos
            final ProductEntity productId1 = createProductEntity("11111111-1111-1111-1111-111111111111");
            final ProductEntity productId2 = createProductEntity("22222222-2222-2222-2222-222222222222");
            
            // Crear categorías con IDs específicos
            final Category category1 = new Category(new EntityId(UUID.randomUUID()), "Makeup");
            final Category category2 = new Category(new EntityId(UUID.randomUUID()), "Eye Makeup");

            // Crear productos de dominio con IDs específicos
            final Product domain1 = new Product(
                    new EntityId(UUID.fromString("765d310a-c838-429c-bd82-3172c2d7f49e")),
                    "Lipstick", "Maybelline", category1, "#FF0000");
            final Product domain2 = new Product(
                    new EntityId(UUID.fromString("cc2217f0-56c7-48f8-beb3-8da8f6f70f23")),
                    "Mascara", "L'Oreal", category2, "#000000");

            // Configurar comportamiento del repositorio
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
    }
}