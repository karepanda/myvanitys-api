package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "product_user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"}, name = "uq_user_product")
    }
)
public class ProductUserEntity {

  @Id
  @Column(name = "product_user_id", nullable = false, unique = true)
  private UUID productUserId;

  @Version
  private Long version;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @OneToMany
  @JoinColumn(name = "product_user_id", referencedColumnName = "product_user_id")
  @ToString.Exclude
  private List<ReviewEntity> reviews;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;
}