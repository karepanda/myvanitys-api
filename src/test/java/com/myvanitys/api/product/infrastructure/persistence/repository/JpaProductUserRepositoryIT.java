package com.myvanitys.api.product.infrastructure.persistence.repository;


import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.Test;
import com.myvanitys.api.common.test.AbstractRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class JpaProductUserRepositoryIT extends AbstractRepositoryIntegrationTest {
    @Autowired
    private JpaProductUserRepository jpaProductUserRepository;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private JpaReviewRepository jpaReviewRepository;

    @Autowired
    private JpaCategoryRepository jpaCategoryRepository;


    @Test
    void shouldSaveAndRetrieveProductUser() {
        // Given
        ProductEntity product = createSampleProduct("Test Product");
        jpaProductRepository.save(product);

        ProductUserEntity productUser = createSampleProductUser(product);
        jpaProductUserRepository.save(productUser);  // Guardar primero el ProductUserEntity

        ReviewEntity review = createSampleReview(productUser);
        jpaReviewRepository.save(review);  // Ahora se guarda la ReviewEntity con la relación establecida

        // Asignar la review al productUser y actualizarlo en la BD
        productUser.setReviews(List.of(review));
        jpaProductUserRepository.save(productUser);

        // When
        Optional<ProductUserEntity> retrievedProductUser = jpaProductUserRepository.findById(productUser.getProductId());

        // Then
        assertThat(retrievedProductUser).isPresent();


    }

    @Test
    void shouldFindProductUserByUserId() {

    }

    @Test
    void shouldDeleteProductUserByProductId() {

    }

    @Test
    void shouldCheckIfProductUserExistsByProductIdAndUserId() {

    }


    private ProductEntity createSampleProduct(String name) {
        ProductEntity product = new ProductEntity();

        CategoryEntity category = new CategoryEntity();
        category.setCategoryId(UUID.randomUUID()); // Asigna un ID
        category.setName("Sample Category");
        category = jpaCategoryRepository.save(category); // Guarda la categoría antes de asignarla al producto

        product.setProductId(UUID.randomUUID());
        product.setName(name);
        product.setBrand("Test Brand");
        product.setColorHex("#FFFFFF");
        product.setCategory(category); // Asigna la categoría guardada

        return product;
    }


    private ReviewEntity createSampleReview(ProductUserEntity productUser) {
        ReviewEntity review = new ReviewEntity();
        review.setComment("Great product!");
        review.setRating(5);
        review.setProductUserEntity(productUser);
        return review;
    }


    private ProductUserEntity createSampleProductUser(ProductEntity product) {
        ProductUserEntity productUser = new ProductUserEntity();
        productUser.setProductId(product.getProductId());
        productUser.setUserId(UUID.randomUUID());
        return productUser;
    }

}