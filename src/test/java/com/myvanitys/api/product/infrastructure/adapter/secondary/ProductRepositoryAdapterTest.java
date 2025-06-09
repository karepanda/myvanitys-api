package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductUserRelationMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ProductUserRepository productUserRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private ProductUserRelationMapper productUserRelationMapper;

  @Mock
  private ReviewMapper reviewMapper;

  @InjectMocks
  private ProductRepositoryAdapter target;

  private Review review;

  private ReviewEntity reviewEntity;

  private Category category;

  private UUID categoryId;

  private EntityId categoryEntityId;

  @BeforeEach
  void setUp() {
    categoryId = UUID.randomUUID();
    categoryEntityId = new EntityId(categoryId);
    category = new Category(categoryEntityId, "Test Category");

    review = Review.createWithExistingId(
        EntityId.newId(),
        EntityId.newId(),
        ReviewDetails.of(3, "Test review", Instant.now(), Instant.now(), null));

    // Create ReviewEntity for mocking
    reviewEntity = new ReviewEntity();
    reviewEntity.setReviewId(review.getId().getValue());
    reviewEntity.setProductUserId(review.getProductUserId().getValue());
    reviewEntity.setRating(3);
    reviewEntity.setComment("Test review");
    reviewEntity.setCreatedAt(Instant.now());
    reviewEntity.setUpdatedAt(Instant.now());
  }

  private ProductEntity createValidProductEntity(UUID productId, String name, String brand) {
    ProductEntity entity = new ProductEntity();
    entity.setProductId(productId);
    entity.setCategoryId(categoryId);
    entity.setName(name != null && !name.trim().isEmpty() ? name : "Default Name");
    entity.setBrand(brand != null && !brand.trim().isEmpty() ? brand : "Default Brand");
    entity.setColorHex("#FFFFFF");
    return entity;
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    void when_givenNewProduct_then_returnsSavedProduct() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          List.of(review),
          Set.of()
      );

      final ProductEntity productEntity = createValidProductEntity(productId, "Test Product", "Test Brand");
      final ProductEntity savedProductEntity = createValidProductEntity(productId, "Test Product", "Test Brand");
      savedProductEntity.setCreatedAt(Instant.now());

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.empty());
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(any(ProductEntity.class))).thenReturn(savedProductEntity);
      when(reviewRepository.findById(review.getId())).thenReturn(Optional.empty());

      // Mock para reconstructProductWithAllData
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(jpaProductRepository.findReviewsByProductId(productId)).thenReturn(List.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);
      when(productUserRepository.findByProductId(productId)).thenReturn(List.of());

      // When
      final Product result = target.save(product);

      // Then
      assertThat(result).isNotNull();
      verify(categoryRepository, times(2)).findById(categoryEntityId);
      verify(jpaProductRepository).findById(productId);
      verify(productMapper).toEntity(product);
      verify(jpaProductRepository).save(any(ProductEntity.class));
      verify(reviewRepository).save(review);
      verify(jpaProductRepository).findReviewsByProductId(productId);
      verify(reviewMapper).toDomain(reviewEntity);
      verify(productUserRepository).findByProductId(productId);
    }

    @Test
    void when_givenExistingProduct_then_doesNotSaveProductEntityAgain() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final EntityId userId = EntityId.newId();

      Set<ProductUserRelation> relations = new HashSet<>();
      relations.add(ProductUserRelation.create(productEntityId, userId));

      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          List.of(review),
          relations
      );

      final ProductEntity existingProductEntity = createValidProductEntity(productId, "Test Product", "Test Brand");
      existingProductEntity.setCreatedAt(Instant.now().minusSeconds(3600));

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(existingProductEntity));
      when(productUserRepository.existsByProductIdAndUserId(productEntityId, userId)).thenReturn(true);
      when(reviewRepository.findById(review.getId())).thenReturn(Optional.empty());

      // Mock para reconstructProductWithAllData
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(jpaProductRepository.findReviewsByProductId(productId)).thenReturn(List.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);
      when(productUserRepository.findByProductId(productId)).thenReturn(List.of(relations.iterator().next()));

      // When
      final Product result = target.save(product);

      // Then
      assertThat(result).isNotNull();
      verify(categoryRepository, times(2)).findById(categoryEntityId);
      verify(jpaProductRepository).findById(productId);
      verify(jpaProductRepository, never()).save(any());
      verify(productUserRepository).existsByProductIdAndUserId(productEntityId, userId);
      verify(reviewRepository).save(review);
    }

    @Test
    void when_productUserRelationDoesNotExist_then_throwsRepositoryResourceNotFoundException() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final EntityId userId = EntityId.newId();

      Set<ProductUserRelation> relations = new HashSet<>();
      relations.add(ProductUserRelation.create(productEntityId, userId));

      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          List.of(review),
          relations
      );

      final ProductEntity existingProductEntity = createValidProductEntity(productId, "Test Product", "Test Brand");

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(existingProductEntity));
      when(productUserRepository.existsByProductIdAndUserId(productEntityId, userId)).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> target.save(product))
          .isInstanceOf(RepositoryResourceNotFoundException.class)
          .hasMessageContaining("ProductUser relation must exist before adding reviews");

      verify(categoryRepository).findById(categoryEntityId);
      verify(productUserRepository).existsByProductIdAndUserId(productEntityId, userId);
      verify(reviewRepository, never()).save(any());
    }

    @Test
    void when_categoryDoesNotExist_then_throwsRepositoryResourceNotFoundException() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          List.of(),
          Set.of()
      );

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> target.save(product))
          .isInstanceOf(RepositoryResourceNotFoundException.class)
          .hasMessageContaining("Category not found");

      verify(categoryRepository).findById(categoryEntityId);
      verify(productMapper, never()).toEntity(any());
      verify(jpaProductRepository, never()).save(any());
    }

    @Test
    void when_dataAccessExceptionOccurs_then_throwsDatabaseException() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          List.of(),
          Set.of()
      );

      final ProductEntity productEntity = createValidProductEntity(productId, "Test Product", "Test Brand");

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.empty());
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(any(ProductEntity.class))).thenThrow(mock(DataAccessException.class));

      // When & Then
      assertThatThrownBy(() -> target.save(product))
          .isInstanceOf(DatabaseException.class)
          .hasMessageContaining("Save product");
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    void when_productExists_then_returnsProduct() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);

      final ProductEntity productEntity = createValidProductEntity(productId, "Test Product", "Test Brand");

      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(jpaProductRepository.findReviewsByProductId(productId)).thenReturn(List.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);
      when(productUserRepository.findByProductId(productId)).thenReturn(List.of());

      // When
      final Optional<Product> result = target.findById(productEntityId);

      // Then
      assertThat(result).isPresent();
      verify(jpaProductRepository).findById(productId);
      verify(categoryRepository).findById(new EntityId(categoryId));
      verify(jpaProductRepository).findReviewsByProductId(productId);
      verify(reviewMapper).toDomain(reviewEntity);
      verify(productUserRepository).findByProductId(productId);
    }

    @Test
    void when_productDoesNotExist_then_returnsEmpty() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);

      when(jpaProductRepository.findById(productId)).thenReturn(Optional.empty());

      // When
      final Optional<Product> result = target.findById(productEntityId);

      // Then
      assertThat(result).isEmpty();
      verify(jpaProductRepository).findById(productId);
      verify(categoryRepository, never()).findById(any());
      verify(jpaProductRepository, never()).findReviewsByProductId(any());
      verify(reviewMapper, never()).toDomain(any());
      verify(productUserRepository, never()).findByProductId(any());
    }
  }

  @Nested
  @DisplayName("findByUserId")
  class FindByUserId {

    @Test
    void when_userHasProducts_then_returnsListOfProducts() {
      // Given
      final UUID userId = UUID.randomUUID();
      final EntityId userEntityId = new EntityId(userId);

      final EntityId productEntityId1 = EntityId.newId();
      final EntityId productEntityId2 = EntityId.newId();

      final ProductEntity productEntity1 = createValidProductEntity(productEntityId1.getValue(), "Product 1", "Brand 1");
      final ProductEntity productEntity2 = createValidProductEntity(productEntityId2.getValue(), "Product 2", "Brand 2");

      when(productUserRepository.findProductIdsByUserId(userEntityId))
          .thenReturn(List.of(productEntityId1, productEntityId2));
      when(jpaProductRepository.findAllById(List.of(productEntityId1.getValue(), productEntityId2.getValue())))
          .thenReturn(List.of(productEntity1, productEntity2));

      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(jpaProductRepository.findReviewsByProductIdAndUserId(any(UUID.class), eq(userId)))
          .thenReturn(List.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);
      when(productUserRepository.findByProductId(any(UUID.class))).thenReturn(List.of());

      // When
      final List<Product> result = target.findByUserId(userId);

      // Then
      assertThat(result).hasSize(2);
      verify(productUserRepository).findProductIdsByUserId(userEntityId);
      verify(jpaProductRepository).findAllById(List.of(
          productEntityId1.getValue(),
          productEntityId2.getValue()));
    }

    @Test
    void when_userHasNoProducts_then_returnsEmptyList() {
      // Given
      final UUID userId = UUID.randomUUID();
      final EntityId userEntityId = new EntityId(userId);

      when(productUserRepository.findProductIdsByUserId(userEntityId))
          .thenReturn(List.of());

      // When
      final List<Product> result = target.findByUserId(userId);

      // Then
      assertThat(result).isEmpty();
      verify(productUserRepository).findProductIdsByUserId(userEntityId);
      verify(jpaProductRepository, never()).findAllById(any());
    }
  }

  @Nested
  @DisplayName("findAll")
  class FindAll {

    @Test
    void when_thereAreProducts_then_returnsListOfAllProducts() {
      // Given
      final EntityId productEntityId1 = EntityId.newId();
      final EntityId productEntityId2 = EntityId.newId();

      final ProductEntity productEntity1 = createValidProductEntity(productEntityId1.getValue(), "Product 1", "Brand 1");
      final ProductEntity productEntity2 = createValidProductEntity(productEntityId2.getValue(), "Product 2", "Brand 2");

      when(jpaProductRepository.findAll()).thenReturn(List.of(productEntity1, productEntity2));
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(jpaProductRepository.findReviewsByProductId(productEntityId1.getValue())).thenReturn(List.of(reviewEntity));
      when(jpaProductRepository.findReviewsByProductId(productEntityId2.getValue())).thenReturn(List.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);
      when(productUserRepository.findByProductId(productEntityId1.getValue())).thenReturn(List.of());
      when(productUserRepository.findByProductId(productEntityId2.getValue())).thenReturn(List.of());

      // When
      final List<Product> result = target.findAll();

      // Then
      assertThat(result).hasSize(2);
      verify(jpaProductRepository).findAll();
      verify(categoryRepository, times(2)).findById(new EntityId(categoryId));
      verify(jpaProductRepository).findReviewsByProductId(productEntityId1.getValue());
      verify(jpaProductRepository).findReviewsByProductId(productEntityId2.getValue());
      verify(reviewMapper, times(2)).toDomain(reviewEntity);
      verify(productUserRepository).findByProductId(productEntityId1.getValue());
      verify(productUserRepository).findByProductId(productEntityId2.getValue());
    }

    @Test
    void when_noProductsExist_then_returnsEmptyList() {
      // Given
      when(jpaProductRepository.findAll()).thenReturn(List.of());

      // When
      final List<Product> result = target.findAll();

      // Then
      assertThat(result).isEmpty();
      verify(jpaProductRepository).findAll();
      verify(categoryRepository, never()).findById(any());
      verify(jpaProductRepository, never()).findReviewsByProductId(any());
      verify(reviewMapper, never()).toDomain(any());
      verify(productUserRepository, never()).findByProductId(any());
    }
  }
}