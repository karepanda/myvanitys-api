package com.myvanitys.api.product.infrastructure.persistence.repository;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class JpaProductUserRepositoryTest {
    @Mock
    private JpaProductUserRepository jpaProductUserRepository;

    private ProductUserEntity productUserEntity;

    @BeforeEach
    void setUp() {
        // Initialize IDs
        productUserEntity = new ProductUserEntity();
        productUserEntity.setProductId(UUID.randomUUID());
        productUserEntity.setUserId(UUID.randomUUID());
        productUserEntity.setProductUserId(UUID.randomUUID());

    }

}