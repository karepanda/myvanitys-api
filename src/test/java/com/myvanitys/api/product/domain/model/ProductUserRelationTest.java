package com.myvanitys.api.product.domain.model;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductUserRelationTest {
    private EntityId id;
    private EntityId productId;
    private EntityId userId;
    private ProductUserRelation relation;

    @BeforeEach
    void setUp() {
        id = new EntityId(UUID.randomUUID());
        productId = new EntityId(UUID.randomUUID());
        userId = new EntityId(UUID.randomUUID());
        relation = new ProductUserRelation(id, productId, userId);
    }

    @Test
    void shouldInitializeFieldsCorrectly() {
        assertThat(relation.getId()).isEqualTo(id);
        assertThat(relation.getProductId()).isEqualTo(productId);
        assertThat(relation.getUserId()).isEqualTo(userId);
        assertThat(relation.getReviewId()).isNull();
    }

    @Test
    void shouldSetReviewId() {
        EntityId reviewId = new EntityId(UUID.randomUUID());
        relation.setReviewId(reviewId);

        assertThat(relation.getReviewId()).isEqualTo(reviewId);
    }

    @Test
    void equalsAndHashCodeShouldWorkBasedOnUserIdAndProductId() {
        ProductUserRelation sameRelation = new ProductUserRelation(new EntityId(UUID.randomUUID()), productId, userId);
        ProductUserRelation differentRelation = new ProductUserRelation(new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()));

        assertThat(relation)
                .isEqualTo(sameRelation)
                .hasSameHashCodeAs(sameRelation);

        assertThat(relation)
                .isNotEqualTo(differentRelation);
    }

    @Test
    void shouldNotBeEqualToNullOrDifferentClass() {
        assertThat(relation).isNotEqualTo(null);
        assertThat(relation).isNotEqualTo("Some String");
    }
}