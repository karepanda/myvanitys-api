package com.myvanitys.api.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddReviewToProductTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Product product;

    @InjectMocks
    private AddReviewToProduct addReviewToProduct;

    private EntityId productId;
    private EntityId userId;
    private ReviewDetails reviewDetails;
    private AddReviewToProductCommand command;

    @BeforeEach
    void setUp() {
        productId = new EntityId(UUID.randomUUID());
        userId = new EntityId(UUID.randomUUID());
        reviewDetails = ReviewDetails.create(4, "Test Comment");
        command = new AddReviewToProductCommand(userId, productId, reviewDetails);
    }

    @Nested
    class Execute {

        @Test
        void when_productExists_then_addsReviewAndSavesProduct() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            // When
            Product result = addReviewToProduct.execute(command);

            // Then
            verify(product).addReviewFromUser(userId, reviewDetails);
            verify(productRepository).save(product);
            assertThat(result).isEqualTo(product);
        }

        @Test
        void when_productDoesNotExist_then_throwsProductNotFoundException() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> addReviewToProduct.execute(command))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found" + productId);
        }
    }

    @Nested
    class FindProductOrThrow {

        @Test
        void when_productExists_then_returnsProduct() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            command = new AddReviewToProductCommand(userId, productId, reviewDetails);

            // When
            Product result = addReviewToProduct.execute(command);

            // Then
            assertThat(result).isEqualTo(product);
            verify(productRepository).findById(productId);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        void when_productDoesNotExist_then_throwsProductNotFoundException() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> addReviewToProduct.execute(command))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found" + productId.getValue());
        
            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any(Product.class));
        }
    }
}