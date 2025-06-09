package com.myvanitys.api.product.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteProductFromUserVanityTest {

  @Mock
  private ProductUserRepository productUserRepository;

  @InjectMocks
  private DeleteProductFromUserVanity deleteProductFromUserVanity;

  private UUID productId;

  private UUID userId;

  private DeleteProductFromUserVanityCommand command;

  private ProductUserRelation productUserRelation;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    userId = UUID.randomUUID();
    command = new DeleteProductFromUserVanityCommand(productId, userId);

    // Usar objeto de dominio en lugar de entidad de infraestructura
    productUserRelation = ProductUserRelation.create(
        new EntityId(productId),
        new EntityId(userId)
    );
  }

  @Test
  @DisplayName("Should delete product from user vanity successfully when product exists in collection")
  void shouldDeleteProductFromUserVanitySuccessfully() {
    // Given
    when(productUserRepository.findByProductIdAndUserId(productId, userId))
        .thenReturn(Optional.of(productUserRelation));

    // When
    assertDoesNotThrow(() -> deleteProductFromUserVanity.execute(command));

    // Then
    verify(productUserRepository).findByProductIdAndUserId(productId, userId);
    verify(productUserRepository).deleteByProductIdAndUserId(productId, userId);
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when product is not in user's vanity collection")
  void shouldThrowProductNotFoundExceptionWhenProductNotInUserVanity() {
    // Given
    when(productUserRepository.findByProductIdAndUserId(productId, userId))
        .thenReturn(Optional.empty());

    // When & Then
    ProductNotFoundException exception = assertThrows(
        ProductNotFoundException.class,
        () -> deleteProductFromUserVanity.execute(command)
    );

    assertEquals("Product is not in user's vanity collection\n", exception.getMessage());
    verify(productUserRepository).findByProductIdAndUserId(productId, userId);
    verify(productUserRepository, never()).deleteByProductIdAndUserId(any(), any());
  }

  @Test
  @DisplayName("Should verify repository method calls in correct order")
  void shouldVerifyRepositoryMethodCallsInCorrectOrder() {
    // Given
    when(productUserRepository.findByProductIdAndUserId(productId, userId))
        .thenReturn(Optional.of(productUserRelation));

    // When
    deleteProductFromUserVanity.execute(command);

    // Then
    var inOrder = inOrder(productUserRepository);
    inOrder.verify(productUserRepository).findByProductIdAndUserId(productId, userId);
    inOrder.verify(productUserRepository).deleteByProductIdAndUserId(productId, userId);
  }

  @Test
  @DisplayName("Should handle different product and user ID combinations")
  void shouldHandleDifferentProductAndUserIdCombinations() {
    // Given
    UUID differentProductId = UUID.randomUUID();
    UUID differentUserId = UUID.randomUUID();
    DeleteProductFromUserVanityCommand differentCommand =
        new DeleteProductFromUserVanityCommand(differentProductId, differentUserId);

    ProductUserRelation differentRelation = ProductUserRelation.create(
        new EntityId(differentProductId),
        new EntityId(differentUserId)
    );

    when(productUserRepository.findByProductIdAndUserId(differentProductId, differentUserId))
        .thenReturn(Optional.of(differentRelation));

    // When
    assertDoesNotThrow(() -> deleteProductFromUserVanity.execute(differentCommand));

    // Then
    verify(productUserRepository).findByProductIdAndUserId(differentProductId, differentUserId);
    verify(productUserRepository).deleteByProductIdAndUserId(differentProductId, differentUserId);
  }

  @Test
  @DisplayName("Should not call delete when product is not found")
  void shouldNotCallDeleteWhenProductNotFound() {
    // Given
    when(productUserRepository.findByProductIdAndUserId(productId, userId))
        .thenReturn(Optional.empty());

    // When
    assertThrows(ProductNotFoundException.class,
        () -> deleteProductFromUserVanity.execute(command));

    // Then
    verify(productUserRepository).findByProductIdAndUserId(productId, userId);
    verify(productUserRepository, never()).deleteByProductIdAndUserId(productId, userId);
  }

  @Test
  @DisplayName("Should handle repository exceptions gracefully")
  void shouldHandleRepositoryExceptionsGracefully() {
    // Given
    when(productUserRepository.findByProductIdAndUserId(productId, userId))
        .thenThrow(new RuntimeException("Database error"));

    // When & Then
    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> deleteProductFromUserVanity.execute(command)
    );

    assertEquals("Database error", exception.getMessage());
    verify(productUserRepository).findByProductIdAndUserId(productId, userId);
    verify(productUserRepository, never()).deleteByProductIdAndUserId(any(), any());
  }

  @Test
  @DisplayName("Should verify exact method parameters are passed to repository")
  void shouldVerifyExactMethodParametersPassedToRepository() {
    // Given
    when(productUserRepository.findByProductIdAndUserId(productId, userId))
        .thenReturn(Optional.of(productUserRelation));

    // When
    deleteProductFromUserVanity.execute(command);

    // Then
    verify(productUserRepository).findByProductIdAndUserId(eq(productId), eq(userId));
    verify(productUserRepository).deleteByProductIdAndUserId(eq(productId), eq(userId));
  }
}