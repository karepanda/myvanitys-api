package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUserRepository extends JpaRepository<ProductUserEntity, UUID> {

  Optional<ProductUserEntity> findByUserIdAndProductId(UUID userId, UUID productId);
}


