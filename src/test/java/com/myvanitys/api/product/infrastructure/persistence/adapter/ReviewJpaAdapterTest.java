package com.myvanitys.api.product.infrastructure.persistence.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.output.ReviewJpaAdapter;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductUserMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewJpaAdapterTest {

  @InjectMocks
  private ReviewJpaAdapter target;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ProductUserRepository productUserRepository;

  @Mock
  private ReviewMapper reviewMapper;

  @Mock
  private ProductUserMapper productUserMapper;

  private EntityId productId;

  private EntityId categoryId;

  private EntityId userId;

  private EntityId reviewId;

  private Category category;

  private Product product;

  private Review review;

  private ReviewEntity reviewEntity;

  private ProductUserEntity productUserEntity;

  @BeforeEach
  void setUp() {
    // Inicializar IDs
    productId = new EntityId(UUID.randomUUID());
    categoryId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    reviewId = new EntityId(UUID.randomUUID());

    // Inicializar objetos de dominio
    category = new Category(categoryId, "Test Category");
    product = new Product(productId, "Test Product", "Test Brand", category, "#FFFFFF");
    review = new Review(reviewId, userId, product, 5, "Great product!");

    // Inicializar entidades
    productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(UUID.randomUUID());
    productUserEntity.setProductId(productId.getValue());
    productUserEntity.setUserId(userId.getValue());

    reviewEntity = new ReviewEntity();
    reviewEntity.setReviewId(reviewId.getValue());
    reviewEntity.setRating(5);
    reviewEntity.setComment("Great product!");
    reviewEntity.setProductUserEntity(productUserEntity);
    reviewEntity.setCreatedAt(new Date());
    reviewEntity.setUpdatedAt(new Date());
  }

  @Nested
  class Save {

    @Test
    void when_givenValidReview_then_reviewIsSaved() {
      // Configurar mocks
      when(productUserRepository.findByProductIdAndUserId(
          product.getId().getValue(),
          userId.getValue()))
          .thenReturn(productUserEntity);

      when(reviewMapper.toEntity(review)).thenReturn(reviewEntity);
      when(reviewRepository.save(reviewEntity)).thenReturn(reviewEntity);
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      // Ejecutar método bajo prueba
      final Review result = target.save(review);

      // Verificar resultados
      assertThat(result).isEqualTo(review);
      verify(productUserRepository).findByProductIdAndUserId(
          product.getId().getValue(),
          userId.getValue());
      verify(reviewMapper).toEntity(review);
      verify(reviewRepository).save(reviewEntity);
      verify(reviewMapper).toDomain(reviewEntity);
    }
  }

  @Nested
  class FindById {

    @Test
    void when_givenExistingId_then_reviewIsReturned() {
      // Configurar mocks
      when(reviewRepository.findById(reviewId.getValue())).thenReturn(Optional.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      // Ejecutar método bajo prueba
      final Optional<Review> result = target.findById(reviewId);

      // Verificar resultados
      assertThat(result).isPresent().contains(review);
      verify(reviewRepository).findById(reviewId.getValue());
      verify(reviewMapper).toDomain(reviewEntity);
    }

    @Test
    void when_givenNonExistingId_then_emptyOptionalIsReturned() {
      // Configurar mocks
      when(reviewRepository.findById(reviewId.getValue())).thenReturn(Optional.empty());

      // Ejecutar método bajo prueba
      final Optional<Review> result = target.findById(reviewId);

      // Verificar resultados
      assertThat(result).isEmpty();
      verify(reviewRepository).findById(reviewId.getValue());
      verify(reviewMapper, never()).toDomain(any());
    }
  }

  @Nested
  class FindByProductId {

    @Test
    void when_givenProductId_then_returnReviewsList() {
      // Configurar mocks
      List<ReviewEntity> reviewEntities = List.of(reviewEntity);
      when(reviewRepository.findByProductUserEntityProductId(productId.getValue()))
          .thenReturn(reviewEntities);
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      // Ejecutar método bajo prueba
      final List<Review> result = target.findByProductId(productId);

      // Verificar resultados
      assertThat(result).isEqualTo(List.of(review));
      verify(reviewRepository).findByProductUserEntityProductId(productId.getValue());
      verify(reviewMapper).toDomain(reviewEntity);
    }

    @Test
    void when_givenProductIdWithoutReviews_then_returnEmptyList() {
      // Configurar mocks
      when(reviewRepository.findByProductUserEntityProductId(productId.getValue()))
          .thenReturn(List.of());

      // Ejecutar método bajo prueba
      final List<Review> result = target.findByProductId(productId);

      // Verificar resultados
      assertThat(result).isEqualTo(List.of());
      verify(reviewRepository).findByProductUserEntityProductId(productId.getValue());
      verify(reviewMapper, never()).toDomain(any());
    }
  }

  @Nested
  class FindByUserId {

    @Test
    void when_givenUserId_then_returnReviewsList() {
      // Configurar mocks
      List<ReviewEntity> reviewEntities = List.of(reviewEntity);
      when(reviewRepository.findByProductUserEntityUserId(userId.getValue()))
          .thenReturn(reviewEntities);
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      // Ejecutar método bajo prueba
      final List<Review> result = target.findByUserId(userId);

      // Verificar resultados
      assertThat(result).isEqualTo(List.of(review));
      verify(reviewRepository).findByProductUserEntityUserId(userId.getValue());
      verify(reviewMapper).toDomain(reviewEntity);
    }

    @Test
    void when_givenUserIdWithoutReviews_then_returnEmptyList() {
      // Configurar mocks
      when(reviewRepository.findByProductUserEntityUserId(userId.getValue()))
          .thenReturn(List.of());

      // Ejecutar método bajo prueba
      final List<Review> result = target.findByUserId(userId);

      // Verificar resultados
      assertThat(result).isEqualTo(List.of());
      verify(reviewRepository).findByProductUserEntityUserId(userId.getValue());
      verify(reviewMapper, never()).toDomain(any());
    }
  }

  @Nested
  class DeleteById {

    @Test
    void when_givenReviewId_then_reviewIsDeleted() {
      // Ejecutar método bajo prueba
      target.deleteById(reviewId);

      // Verificar resultados
      verify(reviewRepository).deleteById(reviewId.getValue());
    }
  }
}