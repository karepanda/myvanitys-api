package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    // ✅ CORREGIDO: Solo 3 parámetros, sin reviewId
    relation = ProductUserRelation.reconstruct(id, productId, userId);
  }

  @Test
  void shouldInitializeFieldsCorrectly() {
    assertThat(relation.getId()).isEqualTo(id);
    assertThat(relation.getProductId()).isEqualTo(productId);
    assertThat(relation.getUserId()).isEqualTo(userId);
  }

  @Test
  void shouldCreateNewRelation() {
    // Test para el método create
    ProductUserRelation newRelation = ProductUserRelation.create(productId, userId);

    assertThat(newRelation.getId()).isNotNull();
    assertThat(newRelation.getProductId()).isEqualTo(productId);
    assertThat(newRelation.getUserId()).isEqualTo(userId);
  }

  @Test
  void equalsAndHashCodeShouldWorkBasedOnUserIdAndProductId() {
    // ✅ CORREGIDO: Solo 3 parámetros
    ProductUserRelation sameRelation = ProductUserRelation.reconstruct(
        new EntityId(UUID.randomUUID()), productId, userId);

    ProductUserRelation differentRelation = ProductUserRelation.reconstruct(
        new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()));

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

  @Test
  void shouldThrowExceptionWhenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> ProductUserRelation.reconstruct(null, productId, userId))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> ProductUserRelation.reconstruct(id, null, userId))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> ProductUserRelation.reconstruct(id, productId, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldBeEqualWhenSameUserAndProduct() {
    ProductUserRelation relation1 = ProductUserRelation.create(productId, userId);
    ProductUserRelation relation2 = ProductUserRelation.create(productId, userId);

    assertThat(relation1).isEqualTo(relation2);
    assertThat(relation1.hashCode()).isEqualTo(relation2.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenDifferentUser() {
    EntityId differentUserId = new EntityId(UUID.randomUUID());
    ProductUserRelation differentUserRelation = ProductUserRelation.create(productId, differentUserId);

    assertThat(relation).isNotEqualTo(differentUserRelation);
  }

  @Test
  void shouldNotBeEqualWhenDifferentProduct() {
    EntityId differentProductId = new EntityId(UUID.randomUUID());
    ProductUserRelation differentProductRelation = ProductUserRelation.create(differentProductId, userId);

    assertThat(relation).isNotEqualTo(differentProductRelation);
  }

  @Test
  void shouldHaveConsistentToString() {
    String toString = relation.toString();

    assertThat(toString).contains("ProductUserRelation");
    assertThat(toString).contains(id.toString());
    assertThat(toString).contains(productId.toString());
    assertThat(toString).contains(userId.toString());
  }

}