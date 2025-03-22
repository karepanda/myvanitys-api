package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

  List<ReviewEntity> findByProductUserEntityProductId(UUID productId);

  List<ReviewEntity> findByProductUserEntityUserId(UUID userId);
}