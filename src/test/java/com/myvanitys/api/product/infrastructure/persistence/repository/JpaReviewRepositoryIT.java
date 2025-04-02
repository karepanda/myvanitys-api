package com.myvanitys.api.product.infrastructure.persistence.repository;

import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.myvanitys.api.common.test.AbstractRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

class JpaReviewRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private JpaReviewRepository jpaReviewRepository;

    @Test
    void findByProductUserEntityProductUserId() {
        // Given
        ReviewEntity review = createReviewEntity();
        jpaReviewRepository.save(review);

        // When
        List<ReviewEntity> foundReviews = jpaReviewRepository.findByProductUserEntityProductUserId(review.getProductUserEntity().getProductUserId());

        // Then
        assertFalse(foundReviews.isEmpty());
        assertEquals(1, foundReviews.size());
        assertEquals(review.getReviewId(), foundReviews.get(0).getReviewId());
    }

    @Test
    void findByProductUserEntityProductId() {
    }

    @Test
    void findByProductUserEntityUserId() {
    }

    @Test
    void existsByReviewIdAndProductUserEntityUserId() {
    }

    @Test
    void findByRating() {
    }

    @Test
    void findByProductUserEntityProductIdAndRatingGreaterThanEqual() {
    }

    private ReviewEntity createReviewEntity() {
        ReviewEntity review = new ReviewEntity();
        review.setReviewId(UUID.randomUUID());
        review.setRating(5);
        review.setComment("Great product!");
        // Set other properties as needed
        return review;
    }
}