package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {
    @InjectMocks
    private ProductMapperImpl productMapper;  // Aquí el target sería productMapper

    @Mock
    private EntityIdMapper entityIdMapper;

    private ProductEntity productEntity;
    private Product product;
    private CategoryEntity categoryEntity;
    private Category category;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        // Inicializar la entidad de categoría
        categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(categoryId);
        categoryEntity.setName("Skincare");

        // Inicializar la entidad de producto
        productEntity = new ProductEntity();
        productEntity.setProductId(productId);
        productEntity.setName("Moisturizer");
        productEntity.setBrand("BrandX");
        productEntity.setColorHex("#FF5733");
        productEntity.setCategory(categoryEntity);

        // Inicializar la categoría
        category = new Category(new EntityId(categoryId), "Skincare");

        // Inicializar el producto
        product = new Product(new EntityId(productId), "Moisturizer", "BrandX", category, "#FF5733");
    }

    @Test
    void testToDomain() {
        // Simular la conversión de EntityId
        doReturn(new EntityId(productId)).when(entityIdMapper).toEntityId(productId);
        doReturn(new EntityId(categoryId)).when(entityIdMapper).toEntityId(categoryId);

        // Realizar la conversión
        Product mappedProduct = productMapper.toDomain(productEntity);

        // Verificar los resultados
        assertNotNull(mappedProduct);
        assertEquals(productId, mappedProduct.getId().getValue());
        assertEquals("Moisturizer", mappedProduct.getName());
        assertEquals("BrandX", mappedProduct.getBrand());
        assertEquals("#FF5733", mappedProduct.getColorHex());
        assertNotNull(mappedProduct.getCategory());
        assertEquals(categoryId, mappedProduct.getCategory().categoryId().getValue());
    }

    @Test
    void testToEntity() {
        // Usar `doReturn()` para asegurar que siempre devuelve los valores correctos
        doReturn(productId).when(entityIdMapper).toUUID(product.getId());
        doReturn(categoryId).when(entityIdMapper).toUUID(category.categoryId());

        // Realizar la conversión
        ProductEntity mappedEntity = productMapper.toEntity(product);

        // Verificar los resultados
        assertNotNull(mappedEntity);
        assertEquals(productId, mappedEntity.getProductId());
        assertEquals("Moisturizer", mappedEntity.getName());
        assertEquals("BrandX", mappedEntity.getBrand());
        assertEquals("#FF5733", mappedEntity.getColorHex());
        assertNotNull(mappedEntity.getCategory());
        assertEquals(categoryId, mappedEntity.getCategory().getCategoryId());
    }

    @Test
    void testCategoryEntityToCategory() {
        // Simular la conversión de CategoryEntity a Category
        when(entityIdMapper.toEntityId(categoryId)).thenReturn(new EntityId(categoryId));

        // Realizar la conversión
        Category mappedCategory = productMapper.categoryEntityToCategory(categoryEntity);

        // Verificar los resultados
        assertNotNull(mappedCategory);
        assertEquals(categoryId, mappedCategory.categoryId().getValue());  // Usar getValue() para obtener el UUID
        assertEquals("Skincare", mappedCategory.name());
    }

    @Test
    void testCategoryToCategoryEntity() {
        // Simular la conversión de Category a CategoryEntity
        when(entityIdMapper.toEntityId(categoryId)).thenReturn(new EntityId(categoryId));

        // Realizar la conversión
        Category mappedCategory = productMapper.categoryEntityToCategory(categoryEntity);

        // Verificar los resultados
        assertNotNull(mappedCategory);
        assertEquals(categoryId, mappedCategory.categoryId().getValue());
        assertEquals("Skincare", mappedCategory.name());
    }

    @Test
    void when_givenNullCategory_then_returnsNull() {
        // Act
        final CategoryEntity categoryEntity = productMapper.mapCategoryToCategoryEntity(null);

        // Assert
        assertNull(categoryEntity);
    }

    @Test
    void when_givenCategoryWithNullCategoryId_then_returnsNull() {
        // Arrange
        final Category category = new Category(null, "Category");

        // Act
        final CategoryEntity categoryEntity = productMapper.mapCategoryToCategoryEntity(category);

        // Assert
        assertNull(categoryEntity);
    }
}
