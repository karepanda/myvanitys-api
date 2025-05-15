package com.myvanitys.api.product.application.command;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddReviewToProductCommandTest {

    @Test
    void constructor_WithAllValues_ShouldCreateCommand() {
        // Arrange
        EntityId userId = new EntityId(UUID.randomUUID());
        EntityId productId = new EntityId(UUID.randomUUID());
        ReviewDetails reviewDetails = ReviewDetails.create(4, "Test Comment");

        // Act
        AddReviewToProductCommand command = new AddReviewToProductCommand(userId, productId, reviewDetails);

        // Assert
        assertNotNull(command);
        assertEquals(userId, command.userId());
        assertEquals(productId, command.productId());
        assertEquals(reviewDetails, command.reviewDetails());
    }

    @Test
    void construct_WithNullReviewDetails_ShouldCreateCommand() {
        // Arrange
        EntityId userId = new EntityId(UUID.randomUUID());
        EntityId productId = new EntityId(UUID.randomUUID());

        // Act
        AddReviewToProductCommand command = new AddReviewToProductCommand(userId, productId, null);

        // Assert
        assertNotNull(command);
        assertEquals(userId, command.userId());
        assertEquals(productId, command.productId());
        assertNull(command.reviewDetails());
    }

    @Test
    void constructor_WithIndividualValues_ShouldCreateCommand() {
        // Arrange
        EntityId userId = new EntityId(UUID.randomUUID());
        EntityId productId = new EntityId(UUID.randomUUID());
        int rating = 4;
        String comment = "Muy buen producto";
        Instant createdAt = Instant.now();

        // Act
        AddReviewToProductCommand command = new AddReviewToProductCommand(
                userId,
                productId,
                rating,
                comment,
                createdAt
        );

        // Assert
        assertNotNull(command);
        assertEquals(userId, command.userId());
        assertEquals(productId, command.productId());
        assertNotNull(command.reviewDetails());
        assertEquals(rating, command.reviewDetails().rating());
        assertEquals(comment, command.reviewDetails().comment());
    }
}