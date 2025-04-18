package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementation of the ProductUserRepository port from the domain This adapter connects the domain with the JPA persistence
 * infrastructure
 */
@Component
public class ProductUserRepositoryAdapter implements ProductUserRepository {

  private final JpaProductUserRepository jpaProductUserRepository;

  public ProductUserRepositoryAdapter(JpaProductUserRepository jpaProductUserRepository) {
    this.jpaProductUserRepository = jpaProductUserRepository;
  }

  @Override
  @Transactional
  public void saveProductUserRelationship(EntityId productId, EntityId userId) {
    UUID productUuid = productId.getValue();
    UUID userUuid = userId.getValue();

    ProductUserEntity entity = ProductUserEntity.builder()
        .productUserId(UUID.randomUUID())
        .productId(productUuid)
        .userId(userUuid)
        .build();

    jpaProductUserRepository.save(entity);
  }

  @Override
  public boolean isUserAssociatedWithProduct(EntityId productId, EntityId userId) {
    UUID productUuid = productId.getValue();
    UUID userUuid = userId.getValue();

    return jpaProductUserRepository.existsByProductIdAndUserId(productUuid, userUuid);
  }

  @Override
  @Transactional
  public void deleteByProductId(EntityId productId) {
    UUID productUuid = productId.getValue();
    jpaProductUserRepository.deleteByProductId(productUuid);
  }

  @Override
  public boolean existsByProductIdAndUserId(EntityId productId, EntityId userId) {
    UUID productUuid = productId.getValue();
    UUID userUuid = userId.getValue();

    return jpaProductUserRepository.existsByProductIdAndUserId(productUuid, userUuid);
  }

  @Override
  public List<EntityId> findProductIdsByUserId(EntityId userId) {
    UUID userUuid = userId.getValue();

    return jpaProductUserRepository.findByUserId(userUuid).stream()
        .map(ProductUserEntity::getProductId)
        .map(EntityId::new)
        .toList();
  }
}