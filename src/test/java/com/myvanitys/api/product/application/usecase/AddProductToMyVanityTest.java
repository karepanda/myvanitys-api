package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.command.AddProductToMyVanityCommand;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddProductToMyVanityTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductUserRepository productUserRepository;

    @InjectMocks
    private AddProductToMyVanity target;

    private EntityId productId;
    private EntityId userId;
    private Product product;
    private AddProductToMyVanityCommand command;

    @BeforeEach
    void setUp() {
        productId = new EntityId(UUID.randomUUID());
        userId = new EntityId(UUID.randomUUID());
        command = new AddProductToMyVanityCommand(productId.getValue(), userId.getValue());

        final EntityId categoryId = new EntityId(UUID.randomUUID());
        final Category category = new Category(categoryId, "Test Category");

        product = Product.reconstruct(
                productId,
                "Test Product",
                "Test Brand",
                category,
                "#FFFFFF",
                null,
                null
        );
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        void when_validCommand_then_addsProductToVanitySuccessfully() {
            // Given
            when(productUserRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(false);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // When
            final Product result = target.execute(command);

            // Then
            assertThat(result).isEqualTo(product);
            verify(productUserRepository).existsByProductIdAndUserId(productId, userId);
            verify(productRepository).findById(productId);
            verify(productUserRepository).saveProductUserRelationship(productId, userId);
        }

        @Test
        void when_productAlreadyAssociatedWithUser_then_throwsIllegalArgumentException() {
            // Given
            when(productUserRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> target.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product is already associated with the user");

            verify(productUserRepository).existsByProductIdAndUserId(productId, userId);
            verify(productRepository, never()).findById(any());
            verify(productUserRepository, never()).saveProductUserRelationship(any(), any());
        }

        @Test
        void when_productDoesNotExist_then_throwsRuntimeException() {
            // Given
            when(productUserRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(false);
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> target.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Product does not exist");

            verify(productUserRepository).existsByProductIdAndUserId(productId, userId);
            verify(productRepository).findById(productId);
            verify(productUserRepository, never()).saveProductUserRelationship(any(), any());
        }


        @Test
        void when_repositoryThrowsException_then_propagatesException() {
            // Given
            final RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(productUserRepository.existsByProductIdAndUserId(productId, userId))
                    .thenThrow(repositoryException);

            // When & Then
            assertThatThrownBy(() -> target.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(productUserRepository).existsByProductIdAndUserId(productId, userId);
            verify(productRepository, never()).findById(any());
            verify(productUserRepository, never()).saveProductUserRelationship(any(), any());
        }

        @Test
        void when_saveProductUserRelationshipFails_then_propagatesException() {
            // Given
            final RuntimeException saveException = new RuntimeException("Failed to save relationship");
            when(productUserRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(false);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            doThrow(saveException).when(productUserRepository).saveProductUserRelationship(productId, userId);

            // When & Then
            assertThatThrownBy(() -> target.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to save relationship");

            verify(productUserRepository).existsByProductIdAndUserId(productId, userId);
            verify(productRepository).findById(productId);
            verify(productUserRepository).saveProductUserRelationship(productId, userId);
        }

        @Test
        void when_transactionRollback_then_handlesGracefully() {
            // Given
            when(productUserRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(false);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            doThrow(new RuntimeException("Transaction rolled back"))
                    .when(productUserRepository).saveProductUserRelationship(productId, userId);

            // When & Then
            assertThatThrownBy(() -> target.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Transaction rolled back");

            verify(productUserRepository).existsByProductIdAndUserId(productId, userId);
            verify(productRepository).findById(productId);
            verify(productUserRepository).saveProductUserRelationship(productId, userId);
        }
    }
}