package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
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
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
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

  @InjectMocks
  private ProductRepositoryAdapter target;

  private Review review;

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
          null
      );

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);
      productEntity.setName("Test Product");

      final ProductEntity savedProductEntity = new ProductEntity();
      savedProductEntity.setProductId(productId);
      savedProductEntity.setCategoryId(categoryId);
      savedProductEntity.setName("Test Product");
      savedProductEntity.setCreatedAt(Instant.now());

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.empty()); // Producto nuevo
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(productEntity)).thenReturn(savedProductEntity);
      when(reviewRepository.findById(review.getId())).thenReturn(Optional.empty()); // Review nueva
      when(productMapper.toDomain(savedProductEntity, category, List.of(review))).thenReturn(product);

      // When
      final Product result = target.save(product);

      // Then
      assertThat(result).isEqualTo(product);
      verify(categoryRepository).findById(categoryEntityId);
      verify(jpaProductRepository).findById(productId);
      verify(productMapper).toEntity(product);
      verify(jpaProductRepository).save(productEntity);
      verify(reviewRepository).save(review);
      verify(productMapper).toDomain(savedProductEntity, category, List.of(review));
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

      final ProductEntity existingProductEntity = new ProductEntity();
      existingProductEntity.setProductId(productId);
      existingProductEntity.setCategoryId(categoryId);
      existingProductEntity.setName("Test Product");
      existingProductEntity.setCreatedAt(Instant.now().minusSeconds(3600));

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(existingProductEntity));
      when(productUserRepository.existsByProductIdAndUserId(productEntityId, userId)).thenReturn(true);
      when(productMapper.toDomain(existingProductEntity, category, List.of(review))).thenReturn(product);

      // When
      final Product result = target.save(product);

      // Then
      assertThat(result).isEqualTo(product);
      verify(categoryRepository).findById(categoryEntityId);
      verify(jpaProductRepository).findById(productId);
      verify(jpaProductRepository, never()).save(any());
      verify(productUserRepository).existsByProductIdAndUserId(productEntityId, userId);
      verify(reviewRepository).save(review);
      verify(productMapper).toDomain(existingProductEntity, category, List.of(review));
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

      final ProductEntity existingProductEntity = new ProductEntity();
      existingProductEntity.setProductId(productId);

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
          null,
          null
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
          null,
          null
      );

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);

      when(categoryRepository.findById(categoryEntityId)).thenReturn(Optional.of(category));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.empty());
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(productEntity)).thenThrow(mock(DataAccessException.class));

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

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);
      productEntity.setName("Test Product");

      final Product expectedProduct = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          List.of(review),
          null
      );

      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(reviewRepository.findByProductId(productEntityId)).thenReturn(List.of(review));
      when(productMapper.toDomain(productEntity, category, List.of(review))).thenReturn(expectedProduct);

      // When
      final Optional<Product> result = target.findById(productEntityId);

      // Then
      assertThat(result)
          .isPresent()
          .contains(expectedProduct);
      verify(jpaProductRepository).findById(productId);
      verify(categoryRepository).findById(new EntityId(categoryId));
      verify(reviewRepository).findByProductId(productEntityId);
      verify(productMapper).toDomain(productEntity, category, List.of(review));
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
      verify(reviewRepository, never()).findByProductId(any());
      verify(productMapper, never()).toDomain(any(), any(), any());
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

      final ProductEntity productEntity1 = new ProductEntity();
      productEntity1.setProductId(productEntityId1.getValue());
      productEntity1.setName("Product 1");
      productEntity1.setCategoryId(categoryId);

      final ProductEntity productEntity2 = new ProductEntity();
      productEntity2.setProductId(productEntityId2.getValue());
      productEntity2.setName("Product 2");
      productEntity2.setCategoryId(categoryId);

      final Product product1 = Product.reconstruct(
          productEntityId1,
          "Product 1",
          "Brand 1",
          category,
          "#FFFFFF",
          List.of(review),
          null
      );

      final Product product2 = Product.reconstruct(
          productEntityId2,
          "Product 2",
          "Brand 2",
          category,
          "#000000",
          List.of(review),
          null
      );

      when(productUserRepository.findProductIdsByUserId(userEntityId))
          .thenReturn(List.of(productEntityId1, productEntityId2));
      when(jpaProductRepository.findAllById(List.of(productEntityId1.getValue(), productEntityId2.getValue())))
          .thenReturn(List.of(productEntity1, productEntity2));
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(reviewRepository.findByUserId(userEntityId)).thenReturn(List.of(review));
      when(productUserRepository.findByProductIdAndUserId(any(UUID.class), eq(userId)))
          .thenReturn(Optional.of(mock(com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity.class)));

      when(productMapper.toDomain(eq(productEntity1), eq(category), anyList())).thenReturn(product1);
      when(productMapper.toDomain(eq(productEntity2), eq(category), anyList())).thenReturn(product2);

      // When
      final List<Product> result = target.findByUserId(userId);

      // Then
      assertThat(result)
          .hasSize(2)
          .containsExactly(product1, product2);

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

      final ProductEntity productEntity1 = new ProductEntity();
      productEntity1.setProductId(productEntityId1.getValue());
      productEntity1.setName("Product 1");
      productEntity1.setCategoryId(categoryId);

      final ProductEntity productEntity2 = new ProductEntity();
      productEntity2.setProductId(productEntityId2.getValue());
      productEntity2.setName("Product 2");
      productEntity2.setCategoryId(categoryId);

      final Product product1 = Product.reconstruct(
          productEntityId1,
          "Product 1",
          "Brand 1",
          category,
          "#FFFFFF",
          List.of(review),
          null
      );

      final Product product2 = Product.reconstruct(
          productEntityId2,
          "Product 2",
          "Brand 2",
          category,
          "#000000",
          List.of(review),
          null
      );

      when(jpaProductRepository.findAll()).thenReturn(List.of(productEntity1, productEntity2));
      when(categoryRepository.findById(new EntityId(categoryId))).thenReturn(Optional.of(category));
      when(reviewRepository.findByProductId(productEntityId1)).thenReturn(List.of(review));
      when(reviewRepository.findByProductId(productEntityId2)).thenReturn(List.of(review));

      when(productMapper.toDomain(eq(productEntity1), eq(category), eq(List.of(review)))).thenReturn(product1);
      when(productMapper.toDomain(eq(productEntity2), eq(category), eq(List.of(review)))).thenReturn(product2);

      // When
      final List<Product> result = target.findAll();

      // Then
      assertThat(result)
          .hasSize(2)
          .containsExactly(product1, product2);

      verify(jpaProductRepository).findAll();
      verify(categoryRepository, times(2)).findById(new EntityId(categoryId));
      verify(reviewRepository).findByProductId(productEntityId1);
      verify(reviewRepository).findByProductId(productEntityId2);
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
      verify(reviewRepository, never()).findByProductId(any());
      verify(productMapper, never()).toDomain(any(), any(), anyList());
    }
  }
}