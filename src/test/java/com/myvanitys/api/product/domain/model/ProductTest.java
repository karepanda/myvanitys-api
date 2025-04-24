package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductTest {

    private Product target;
    private Category category;
    private Review review1;
    private Review review2;
    private Review review3;

    @BeforeEach
    void setUp() {
        EntityId productId = new EntityId(UUID.randomUUID());
        EntityId productUserId = new EntityId(UUID.randomUUID());
        EntityId userId = new EntityId(UUID.randomUUID());
        category = new Category(new EntityId(UUID.randomUUID()), "Test Category");
        
        target = new Product(productId, "Test Product", "Test Brand", category, "#FFFFFF");

        review1 = new Review(new EntityId(UUID.randomUUID()), userId, productUserId, 4, "Review 1");
        review2 = new Review(new EntityId(UUID.randomUUID()), userId, productUserId, 5, "Review 2");
        review3 = new Review(new EntityId(UUID.randomUUID()), userId, productUserId, 3, "Review 3");
    }

    @Nested
    class Constructor {

    @Test
    void when_validParameters_then_objectCreated() {
      final EntityId id = new EntityId(UUID.randomUUID());
      final String name = "Test Product";
      final String brand = "Test Brand";
      final Category testCategory = new Category(new EntityId(UUID.randomUUID()), "Test Category");
      final String colorHex = "#FFFFFF";

      final Product result = new Product(id, name, brand, testCategory, colorHex);

      assertThat(result.getId()).isEqualTo(id);
      assertThat(result.getName()).isEqualTo(name);
      assertThat(result.getBrand()).isEqualTo(brand);
      assertThat(result.getCategory()).isEqualTo(testCategory);
      assertThat(result.getColorHex()).isEqualTo(colorHex);
      assertThat(result.getAverageRating()).isZero();
    }

    @Test
    void when_emptyName_then_throwException() {
      assertThatThrownBy(() -> new Product(new EntityId(UUID.randomUUID()), "", "Brand", category, "#FFFFFF"))
          .isInstanceOf(ProductValidationException.class)
          .hasMessageContaining("name cannot be empty");
    }

    @Test
    void when_nullName_then_throwException() {
      assertThatThrownBy(() -> new Product(new EntityId(UUID.randomUUID()), null, "Brand", category, "#FFFFFF"))
          .isInstanceOf(ProductValidationException.class)
          .hasMessageContaining("name cannot be empty");
    }

    @Test
    void when_emptyBrand_then_throwException() {
      assertThatThrownBy(() -> new Product(new EntityId(UUID.randomUUID()), "Product", "", category, "#FFFFFF"))
          .isInstanceOf(ProductValidationException.class)
          .hasMessageContaining("brand cannot be empty");
    }

    @Test
    void when_invalidColorHex_then_throwException() {
      assertThatThrownBy(() -> new Product(new EntityId(UUID.randomUUID()), "Product", "Brand", category, "INVALID"))
          .isInstanceOf(ProductValidationException.class)
          .hasMessageContaining("Invalid color hex format");
    }
  }

    @Nested
    class GetAverageRating {

        @Test
        void when_noReviews_then_returnZero() {
            assertThat(target.getAverageRating()).isZero();
        }

        @Test
        void when_hasReviews_then_calculateAverage() {
            target.addReview(review1); // 4
            target.addReview(review2); // 5
            target.addReview(review3); // 3

            assertThat(target.getAverageRating()).isEqualTo(4);
        }
    }

    @Nested
    class AddReview {

        @Test
        void when_validReview_then_addToList() {
            target.addReview(review1);

            assertThat(target.getReviews()).hasSize(1);
            assertThat(target.getReviews()).contains(review1);
        }

        @Test
        void when_duplicateReview_then_addOnlyOnce() {
            target.addReview(review1);
            target.addReview(review1);

            assertThat(target.getReviews()).hasSize(1);
        }

        @Test
        void when_nullReview_then_throwException() {
            assertThatThrownBy(() -> target.addReview(null))
                .isInstanceOf(ProductValidationException.class)
                .hasMessageContaining("Review cannot be null");
        }
    }

    @Nested
    class RemoveReview {

        @Test
        void when_existingReview_then_removeFromList() {
            target.addReview(review1);
            target.addReview(review2);

            target.removeReview(review1);

            assertThat(target.getReviews()).hasSize(1);
            assertThat(target.getReviews()).doesNotContain(review1);
            assertThat(target.getReviews()).contains(review2);
        }

        @Test
        void when_nonExistingReview_then_doNothing() {
            target.addReview(review1);

            target.removeReview(review2);

            assertThat(target.getReviews()).hasSize(1);
            assertThat(target.getReviews()).contains(review1);
        }

        @Test
        void when_removeReview_then_updateAverage() {
            target.addReview(review1); // 4
            target.addReview(review2); // 5
            target.addReview(review3); // 3

            target.removeReview(review1);

            assertThat(target.getAverageRating()).isEqualTo(4);
        }
    }

    @Nested
    class UpdateDetails {

        @Test
        void when_validParameters_then_updateFields() {
            final String newName = "New Name";
            final String newBrand = "New Brand";
            final Category newCategory = new Category(new EntityId(UUID.randomUUID()), "New Category");
            final String newColorHex = "#000000";

            target.updateDetails(newName, newBrand, newCategory, newColorHex);

            assertThat(target.getName()).isEqualTo(newName);
            assertThat(target.getBrand()).isEqualTo(newBrand);
            assertThat(target.getCategory()).isEqualTo(newCategory);
            assertThat(target.getColorHex()).isEqualTo(newColorHex);
        }

        @Test
        void when_invalidParameters_then_throwException() {
            assertThatThrownBy(() -> target.updateDetails("", "Brand", category, "#FFFFFF"))
                .isInstanceOf(ProductValidationException.class)
                .hasMessageContaining("name cannot be empty");
        }
    }

    @Nested
    class Equals {

        @Test
        void when_sameId_then_equal() {
            final Product sameIdProduct = new Product(target.getId(), "Different Name", "Different Brand", category, "#000000");

            assertThat(target).isEqualTo(sameIdProduct);
        }

        @Test
        void when_differentId_then_notEqual() {
            final Product differentIdProduct = new Product(new EntityId(UUID.randomUUID()), "Same Name", "Same Brand", category, "#FFFFFF");

            assertThat(target).isNotEqualTo(differentIdProduct);
        }

        @Test
        void when_nullObject_then_notEqual() {
            assertThat(target).isNotEqualTo(null);
        }

        @Test
        void when_differentClass_then_notEqual() {
            assertThat(target).isNotEqualTo(new Object());
        }

        @Test
        void when_sameObject_then_equal() {
            assertThat(target).isEqualTo(target);
        }
    }

    @Nested
    class HashCode {

        @Test
        void when_sameId_then_sameHashCode() {
            final Product sameIdProduct = new Product(target.getId(), "Different Name", "Different Brand", category, "#000000");

            assertThat(target).hasSameHashCodeAs(sameIdProduct);
        }

        @Test
        void when_differentId_then_differentHashCode() {
            final Product differentIdProduct = new Product(new EntityId(UUID.randomUUID()), "Same Name", "Same Brand", category, "#FFFFFF");

            assertThat(target.hashCode()).isNotEqualTo(differentIdProduct.hashCode());
        }
    }

    @Nested
    class ToString {
        
        @Test
        void should_returnStringRepresentation() {
            String result = target.toString();
            
            assertThat(result)
                .contains(target.getId().toString())
                .contains(target.getName())
                .contains(target.getBrand())
                .contains(target.getCategory().toString())
                .contains(target.getColorHex());
        }
    }
}