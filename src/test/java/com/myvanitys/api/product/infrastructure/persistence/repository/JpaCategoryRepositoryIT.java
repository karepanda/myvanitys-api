package com.myvanitys.api.product.infrastructure.persistence.repository;


import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.common.test.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;



class JpaCategoryRepositoryIT extends AbstractRepositoryIntegrationTest {
    @Autowired
    private JpaCategoryRepository jpaCategoryRepository;


    @Test
    void shouldSaveAndRetrieveCategory() {
        // Given
        CategoryEntity category = createSampleCategory("Skincare");

        // When
        CategoryEntity savedCategory = jpaCategoryRepository.save(category);
        Optional<CategoryEntity> retrievedCategory = jpaCategoryRepository.findById(savedCategory.getCategoryId());

        // Then
        assertThat(retrievedCategory).isPresent();
        assertThat(retrievedCategory.get().getName()).isEqualTo("Skincare");
    }

    @Test
    void shouldFindCategoryByName() {
        // Given
        CategoryEntity category = createSampleCategory("Makeup");
        jpaCategoryRepository.save(category);

        // When
        Optional<CategoryEntity> foundCategory = jpaCategoryRepository.findByName("Makeup");

        // Then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Makeup");
    }

    @Test
    void shouldNotFindCategoryByNonExistentName() {

        // When
        Optional<CategoryEntity> foundCategory = jpaCategoryRepository.findByName("NonExistentCategory");

        // Then
        assertThat(foundCategory).isNotPresent();
    }

    @Test
    void shouldDeleteCategory() {
        // Given
        CategoryEntity category = createSampleCategory("ToDelete");
        CategoryEntity savedCategory = jpaCategoryRepository.save(category);
        UUID categoryId = savedCategory.getCategoryId();

        // When
        jpaCategoryRepository.deleteById(categoryId);
        Optional<CategoryEntity> deletedCategory = jpaCategoryRepository.findById(categoryId);

        // Then
        assertThat(deletedCategory).isEmpty();
    }

    private CategoryEntity createSampleCategory(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        return category;
    }
}