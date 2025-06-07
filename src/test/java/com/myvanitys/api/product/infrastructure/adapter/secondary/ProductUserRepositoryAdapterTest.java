package com.myvanitys.api.product.infrastructure.adapter.secondary;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductUserRepositoryAdapterTest {

  @InjectMocks
  private ProductUserRepositoryAdapter target;

  @Mock
  private JpaProductUserRepository jpaProductUserRepository;

  private EntityId productId;

  private EntityId userId;

  private ProductUserEntity productUserEntity;

  @BeforeEach
  void setUp() {
    productId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());

    productUserEntity = ProductUserEntity.builder()
        .productUserId(UUID.randomUUID())
        .productId(productId.getValue())
        .userId(userId.getValue())
        .reviews(new ArrayList<>())
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  @Nested
  class SaveProductUserRelationshipTest {

    @Test
    void shouldSaveProductUserRelationshipWhenNotExists() {
      // Arrange
      when(jpaProductUserRepository.existsByProductIdAndUserId(
          productId.getValue(), userId.getValue())).thenReturn(false);

      // Act
      target.saveProductUserRelationship(productId, userId);

      // Assert
      verify(jpaProductUserRepository).existsByProductIdAndUserId(
          productId.getValue(), userId.getValue());
      verify(jpaProductUserRepository).save(any(ProductUserEntity.class));
    }

    @Test
    void shouldNotSaveProductUserRelationshipWhenAlreadyExists() {
      // Arrange
      when(jpaProductUserRepository.existsByProductIdAndUserId(
          productId.getValue(), userId.getValue())).thenReturn(true);

      // Act
      target.saveProductUserRelationship(productId, userId);

      // Assert
      verify(jpaProductUserRepository).existsByProductIdAndUserId(
          productId.getValue(), userId.getValue());
      verify(jpaProductUserRepository, never()).save(any(ProductUserEntity.class));
    }
  }

  @Nested
  class IsUserAssociatedWithProductTest {

    @Test
    void shouldReturnTrueIfUserIsAssociatedWithProduct() {
      // Arrange
      when(jpaProductUserRepository.existsByProductIdAndUserId(productId.getValue(), userId.getValue()))
          .thenReturn(true);

      // Act
      boolean result = target.isUserAssociatedWithProduct(productId, userId);

      // Assert
      assertThat(result).isTrue();
      verify(jpaProductUserRepository).existsByProductIdAndUserId(productId.getValue(), userId.getValue());
    }

    @Test
    void shouldReturnFalseIfUserIsNotAssociatedWithProduct() {
      // Arrange
      when(jpaProductUserRepository.existsByProductIdAndUserId(productId.getValue(), userId.getValue()))
          .thenReturn(false);

      // Act
      boolean result = target.isUserAssociatedWithProduct(productId, userId);

      // Assert
      assertThat(result).isFalse();
      verify(jpaProductUserRepository).existsByProductIdAndUserId(productId.getValue(), userId.getValue());
    }
  }

  @Nested
  class DeleteByProductIdTest {

    @Test
    void shouldDeleteByProductId() {
      // Act
      target.deleteByProductId(productId);

      // Assert
      verify(jpaProductUserRepository).deleteByProductId(productId.getValue());
    }
  }

  @Nested
  class ExistsByProductIdAndUserIdTest {

    @Test
    void shouldReturnTrueIfProductAndUserExist() {
      // Arrange
      when(jpaProductUserRepository.existsByProductIdAndUserId(productId.getValue(), userId.getValue()))
          .thenReturn(true);

      // Act
      boolean exists = target.existsByProductIdAndUserId(productId, userId);

      // Assert
      assertThat(exists).isTrue();
      verify(jpaProductUserRepository).existsByProductIdAndUserId(productId.getValue(), userId.getValue());
    }

    @Test
    void shouldReturnFalseIfProductAndUserDoNotExist() {
      // Arrange
      when(jpaProductUserRepository.existsByProductIdAndUserId(productId.getValue(), userId.getValue()))
          .thenReturn(false);

      // Act
      boolean exists = target.existsByProductIdAndUserId(productId, userId);

      // Assert
      assertThat(exists).isFalse();
      verify(jpaProductUserRepository).existsByProductIdAndUserId(productId.getValue(), userId.getValue());
    }
  }

  @Nested
  class FindProductIdsByUserIdTest {

    @Test
    void shouldReturnProductIdsByUserId() {
      // Arrange
      UUID anotherProductId = UUID.randomUUID();
      List<ProductUserEntity> entities = List.of(
          productUserEntity,
          ProductUserEntity.builder()
              .productUserId(UUID.randomUUID())
              .productId(anotherProductId)
              .userId(userId.getValue())
              .reviews(new ArrayList<>())
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build()
      );

      when(jpaProductUserRepository.findByUserId(userId.getValue()))
          .thenReturn(entities);

      // Act
      List<EntityId> result = target.findProductIdsByUserId(userId);

      // Assert
      assertThat(result).hasSize(2);
      assertThat(result).extracting(EntityId::getValue).containsExactlyInAnyOrder(
          productUserEntity.getProductId(),
          anotherProductId
      );
      verify(jpaProductUserRepository).findByUserId(userId.getValue());
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsFoundForUser() {
      // Arrange
      when(jpaProductUserRepository.findByUserId(userId.getValue()))
          .thenReturn(Collections.emptyList());

      // Act
      List<EntityId> result = target.findProductIdsByUserId(userId);

      // Assert
      assertThat(result).isEmpty();
      verify(jpaProductUserRepository).findByUserId(userId.getValue());
    }
  }

  @Nested
  class DeleteByProductIdAndUserIdTest {

    @Test
    void shouldDeleteByProductIdAndUserId() {
      // Act
      target.deleteByProductIdAndUserId(productId.getValue(), userId.getValue());

      // Assert
      verify(jpaProductUserRepository).deleteByProductIdAndUserId(productId.getValue(), userId.getValue());
    }

    @Test
    void shouldHandleDeleteWhenRelationshipDoesNotExist() {
      // Arrange
      doNothing().when(jpaProductUserRepository).deleteByProductIdAndUserId(productId.getValue(), userId.getValue());

      // Act
      target.deleteByProductIdAndUserId(productId.getValue(), userId.getValue());

      // Assert
      verify(jpaProductUserRepository).deleteByProductIdAndUserId(productId.getValue(), userId.getValue());
    }
  }
}