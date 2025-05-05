package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    private ProductMapper productMapper;
    private ProductEntity productEntity;
    private Category category;
    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
        
        productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        
        productEntity = new ProductEntity();
        productEntity.setProductId(productId);
        productEntity.setName("TestProduct");
        productEntity.setBrand("TestBrand");
        productEntity.setColorHex("#000000");
        productEntity.setCategoryId(categoryId);

        category = new Category(new EntityId(categoryId), "TestCategory");
        
        product = Product.reconstruct(
            new EntityId(productId),
            "TestProduct",
            "TestBrand",
            category,
            "#000000",
            null,
            null
        );
    }

    @Test
    void shouldMapToDomain() {
        // When
        Product result = productMapper.toDomain(productEntity, category);

        // Then
        assertNotNull(result);
        assertEquals(productEntity.getProductId(), result.getId().getValue());
        assertEquals(productEntity.getName(), result.getName());
        assertEquals(productEntity.getBrand(), result.getBrand());
        assertEquals(productEntity.getColorHex(), result.getColorHex());
        assertEquals(category, result.getCategory());
    }

    @Test
    void shouldMapToNewDomainProduct() {
        // When
        Product result = productMapper.toNewDomainProduct(productEntity);

        // Then
        assertNotNull(result);
        assertEquals(productEntity.getName(), result.getName());
        assertEquals(productEntity.getBrand(), result.getBrand());
        assertEquals(productEntity.getColorHex(), result.getColorHex());
        assertNull(result.getCategory());
    }

    @Test
    void shouldMapToEntity() {
        // When
        ProductEntity result = productMapper.toEntity(product);

        // Then
        assertNotNull(result);
        assertEquals(product.getId().getValue(), result.getProductId());
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getBrand(), result.getBrand());
        assertEquals(product.getColorHex(), result.getColorHex());
        assertEquals(product.getCategory().categoryId().getValue(), result.getCategoryId());
    }

    @Test
    void shouldMapToDomainList() {
        // Given
        List<ProductEntity> entities = List.of(productEntity);

        // When
        List<Product> results = productMapper.toDomainList(entities, category);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(productEntity.getProductId(), results.get(0).getId().getValue());
    }

    @Test
    void shouldMapToNewDomainProductList() {
        // Given
        List<ProductEntity> entities = List.of(productEntity);

        // When
        List<Product> results = productMapper.toNewDomainProductList(entities);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(productEntity.getName(), results.get(0).getName());
    }

    @Test
    void shouldMapToEntityList() {
        // Given
        List<Product> products = List.of(product);

        // When
        List<ProductEntity> results = productMapper.toEntityList(products);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(product.getId().getValue(), results.get(0).getProductId());
    }

    @Test
    void shouldMapToDomainWithRelations() {
        // Given
        UUID productUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        
        ProductUserEntity productUserEntity = new ProductUserEntity();
        productUserEntity.setProductUserId(productUserId);
        productUserEntity.setProductId(productEntity.getProductId());
        productUserEntity.setUserId(UUID.randomUUID());
        
        ReviewEntity reviewEntity = ReviewEntity.builder()
            .reviewId(reviewId)
            .productUserId(productUserId)
            .rating(5)
            .comment("Great product")
            .createdAt(Instant.now())
            .build();
        
        productUserEntity.setReviews(List.of(reviewEntity));

        // When
        Product result = productMapper.toDomainWithRelations(productEntity, List.of(productUserEntity), category);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUserRelations());
        assertFalse(result.getUserRelations().isEmpty());
        assertNotNull(result.getReviews());
        assertFalse(result.getReviews().isEmpty());
    }

    @Test
    void shouldMapToProductUserRelation() {
        // Given
        UUID productUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProductUserEntity productUserEntity = new ProductUserEntity();
        productUserEntity.setProductUserId(productUserId);
        productUserEntity.setProductId(productId);
        productUserEntity.setUserId(userId);

        ReviewEntity reviewEntity = ReviewEntity.builder()
            .reviewId(reviewId)
            .build();
        productUserEntity.setReviews(List.of(reviewEntity));

        // When
        ProductUserRelation result = productMapper.toProductUserRelation(productUserEntity);

        // Then
        assertNotNull(result);
        assertEquals(productUserId, result.getId().getValue());
        assertEquals(productId, result.getProductId().getValue());
        assertEquals(userId, result.getUserId().getValue());
        assertEquals(reviewId, result.getReviewId().getValue());
    }

    @Test
    void shouldMapToReview() {
        // Given
        UUID reviewId = UUID.randomUUID();
        EntityId productUserId = new EntityId(UUID.randomUUID());
        Instant now = Instant.now();

        ReviewEntity reviewEntity = ReviewEntity.builder()
            .reviewId(reviewId)
            .rating(5)
            .comment("Great review")
            .createdAt(now)
            .build();

        // When
        Review result = productMapper.toReview(reviewEntity, productUserId);

        // Then
        assertNotNull(result);
        assertEquals(reviewId, result.getId().getValue());
        assertEquals(productUserId, result.getProductUserId());
        assertEquals(5, result.getRating());
        assertEquals("Great review", result.getComment());
        assertEquals(now, result.getCreatedAt());
    }

    @Test
    void shouldMapToReviewEntity() {
        // Given
        UUID reviewId = UUID.randomUUID();
        EntityId productUserId = new EntityId(UUID.randomUUID());
        Instant now = Instant.now();

        ReviewDetails details = ReviewDetails.of(5, "Great review", now, now, null);
        Review review = Review.createWithExistingId(new EntityId(reviewId), productUserId, details);

        // When
        ReviewEntity result = productMapper.toReviewEntity(review);

        // Then
        assertNotNull(result);
        assertEquals(reviewId, result.getReviewId());
        assertEquals(5, result.getRating());
        assertEquals("Great review", result.getComment());
        assertEquals(now, result.getCreatedAt());
    }

    @Test
    void shouldMapToProductUserEntityList() {
        // Given
        UUID productUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();

        Set<ProductUserRelation> relations = new HashSet<>();
        relations.add(ProductUserRelation.reconstruct(
            new EntityId(productUserId),
            new EntityId(productId),
            new EntityId(UUID.randomUUID()),
            new EntityId(reviewId)
        ));

        ReviewDetails details = ReviewDetails.of(5, "Great review", now, now, null);
        List<Review> reviews = List.of(
            Review.createWithExistingId(new EntityId(reviewId), new EntityId(productUserId), details)
        );

        product = Product.reconstruct(
            new EntityId(productId),
            "TestProduct",
            "TestBrand",
            category,
            "#000000",
            reviews,
            relations
        );

        // When
        List<ProductUserEntity> results = productMapper.toProductUserEntityList(product);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        ProductUserEntity result = results.get(0);
        assertEquals(productUserId, result.getProductUserId());
        assertFalse(result.getReviews().isEmpty());
        assertEquals(reviewId, result.getReviews().get(0).getReviewId());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertNull(productMapper.toDomain(null, category));
        assertNull(productMapper.toNewDomainProduct(null));
        assertNull(productMapper.toEntity(null));
        assertTrue(productMapper.toDomainList(null, category).isEmpty());
        assertTrue(productMapper.toNewDomainProductList(null).isEmpty());
        assertTrue(productMapper.toEntityList(null).isEmpty());
        assertNull(productMapper.toDomainWithRelations(null, null, category));
        assertNull(productMapper.toProductUserRelation(null));
        assertNull(productMapper.toReview(null, null));
        assertNull(productMapper.toReviewEntity(null));
        assertTrue(productMapper.toProductUserEntityList(null).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsNull() {
        assertThrows(NullPointerException.class, () -> productMapper.toDomain(productEntity, null));
        assertThrows(NullPointerException.class, () -> productMapper.toDomainList(List.of(productEntity), null));
        assertThrows(NullPointerException.class, () -> productMapper.toDomainWithRelations(productEntity, new ArrayList<>(), null));
    }
}