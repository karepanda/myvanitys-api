package com.myvanitys.api.product.infrastructure.persistence.repository;


import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JpaCategoryRepositoryTest {
    @Mock
    private JpaCategoryRepository jpaCategoryRepository ;

    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        // Initialize IDs
        category = new CategoryEntity();
        category.setCategoryId(UUID.randomUUID());
        category.setName("Skincare");
    }

    @Test
    void shouldSaveAndRetrieveCategory() {
       //Arrange
        when(jpaCategoryRepository.save(category)).thenReturn(category);
        when(jpaCategoryRepository.findById(category.getCategoryId())).thenReturn(Optional.of(category));

        //Act
        CategoryEntity savedCategory = jpaCategoryRepository.save(category);
        Optional<CategoryEntity> retrievedCategory = jpaCategoryRepository.findById(savedCategory.getCategoryId());

        //Assert
        // Assert
        assertThat(retrievedCategory).isPresent();
        assertThat(retrievedCategory.get().getName()).isEqualTo("Skincare");
        verify(jpaCategoryRepository, times(1)).save(category);
        verify(jpaCategoryRepository, times(1)).findById(category.getCategoryId());
    }

    @Test
    void shouldFindCategoryByName(){
        //Arrange
        when(jpaCategoryRepository.findByName("Makeup")).thenReturn(Optional.of(category));

        //Act
        Optional<CategoryEntity> foundCategory = jpaCategoryRepository.findByName("Makeup");

        //Assert
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Skincare");
        verify(jpaCategoryRepository, times(1)).findByName("Makeup");
    }

    @Test
    void shouldNotFindCategoryByNonExistentName(){
        //Arrange
        when(jpaCategoryRepository.findByName("NonExistentCategory")).thenReturn(Optional.empty());

        //Act
        Optional<CategoryEntity> foundCategory = jpaCategoryRepository.findByName("NonExistentCategory");

        //Assert
        assertThat(foundCategory).isNotPresent();
        verify(jpaCategoryRepository, times(1)).findByName("NonExistentCategory");
    }

    @Test
    void shouldDeleteCategory() {
        //Arrange
        UUID categoryId = category.getCategoryId();
        doNothing().when(jpaCategoryRepository).deleteById(categoryId);
        when(jpaCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        //Act
        jpaCategoryRepository.deleteById(categoryId);
        Optional<CategoryEntity> deletedCategory = jpaCategoryRepository.findById(categoryId);

        //Assert
        assertThat(deletedCategory).isEmpty();
        verify(jpaCategoryRepository, times(1)).deleteById(categoryId);
        verify(jpaCategoryRepository, times(1)).findById(categoryId);
    }
}