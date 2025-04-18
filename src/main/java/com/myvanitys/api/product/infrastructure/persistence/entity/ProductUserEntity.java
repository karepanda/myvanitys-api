package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "product_user")
public class ProductUserEntity {

  @Id
  @Column(name = "product_user_id", nullable = false, unique = true)
  private UUID productUserId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "product_id")
  private UUID productId;

  @OneToMany(mappedBy = "productUserEntity", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<ReviewEntity> reviews;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

}