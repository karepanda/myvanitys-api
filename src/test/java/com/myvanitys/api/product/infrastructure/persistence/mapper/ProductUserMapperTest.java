package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ExtendWith(MockitoExtension.class)
class ProductUserMapperTest {

    private ProductUserMapper target;

    @BeforeEach
    void setUp() {
        target = new ProductUserMapper() {};
    }

    @Nested
    class CreateProductUserEntity {
        @Test
        void when_givenValidEntityIds_then_returnProductUserEntity() {
            // Arrange
            final UUID userIdValue = UUID.randomUUID();
            final UUID productIdValue = UUID.randomUUID();
            final EntityId userId = new EntityId(userIdValue);
            final EntityId productId = new EntityId(productIdValue);

            // Act
            final ProductUserEntity result = target.createProductUserEntity(userId, productId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userIdValue);
            assertThat(result.getProductId()).isEqualTo(productIdValue);
        }
    }
}