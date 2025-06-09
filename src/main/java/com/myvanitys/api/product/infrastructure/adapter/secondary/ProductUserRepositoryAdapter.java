package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductUserRelationMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class ProductUserRepositoryAdapter implements ProductUserRepository {

  private final JpaProductUserRepository jpaProductUserRepository;

  private final ProductUserRelationMapper mapper;

  @Override
  @Transactional
  public void saveProductUserRelationship(EntityId productId, EntityId userId) {
    try {
      UUID productUuid = productId.getValue();
      UUID userUuid = userId.getValue();

      if (!jpaProductUserRepository.existsByProductIdAndUserId(productUuid, userUuid)) {
        ProductUserEntity entity = ProductUserEntity.builder()
            .productUserId(UUID.randomUUID())
            .productId(productUuid)
            .userId(userUuid)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        jpaProductUserRepository.save(entity);
        log.debug("Saved new product-user relationship: Product={}, User={}",
            productUuid, userUuid);
      }
    } catch (DataAccessException e) {
      log.error("Error saving product-user relationship: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean isUserAssociatedWithProduct(EntityId productId, EntityId userId) {
    try {
      UUID productUuid = productId.getValue();
      UUID userUuid = userId.getValue();
      return jpaProductUserRepository.existsByProductIdAndUserId(productUuid, userUuid);
    } catch (DataAccessException e) {
      log.error("Error checking user association with product: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  public void deleteByProductId(EntityId productId) {
    try {
      UUID productUuid = productId.getValue();
      jpaProductUserRepository.deleteByProductId(productUuid);
      log.debug("Deleted all relations for product: {}", productUuid);
    } catch (DataAccessException e) {
      log.error("Error deleting relations by product ID: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean existsByProductIdAndUserId(EntityId productId, EntityId userId) {
    try {
      UUID productUuid = productId.getValue();
      UUID userUuid = userId.getValue();
      return jpaProductUserRepository.existsByProductIdAndUserId(productUuid, userUuid);
    } catch (DataAccessException e) {
      log.error("Error checking existence by product and user ID: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<EntityId> findProductIdsByUserId(EntityId userId) {
    try {
      UUID userUuid = userId.getValue();
      return jpaProductUserRepository.findByUserId(userUuid).stream()
          .map(ProductUserEntity::getProductId)
          .map(EntityId::new)
          .toList();
    } catch (DataAccessException e) {
      log.error("Error finding product IDs by user ID: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<ProductUserRelation> findByProductId(UUID productId) {
    try {
      List<ProductUserEntity> entities = jpaProductUserRepository.findByProductId(productId);
      return mapper.toDomainList(entities);
    } catch (DataAccessException e) {
      log.error("Error finding relations by product ID: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<ProductUserRelation> findByUserId(UUID userId) {
    try {
      List<ProductUserEntity> entities = jpaProductUserRepository.findByUserId(userId);
      return mapper.toDomainList(entities);
    } catch (DataAccessException e) {
      log.error("Error finding relations by user ID: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public Optional<ProductUserRelation> findByProductIdAndUserId(UUID productId, UUID userId) {
    try {
      Optional<ProductUserEntity> entity = jpaProductUserRepository.findByProductIdAndUserId(productId, userId);
      return entity.map(mapper::toDomain);
    } catch (DataAccessException e) {
      log.error("Error finding relation by product and user ID: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  public void deleteByProductIdAndUserId(UUID productId, UUID userId) {
    try {
      jpaProductUserRepository.deleteByProductIdAndUserId(productId, userId);
      log.debug("Deleted relation: Product={}, User={}", productId, userId);
    } catch (DataAccessException e) {
      log.error("Error deleting relation by product and user ID: {}", e.getMessage(), e);
      throw e;
    }
  }
}