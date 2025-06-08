package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for reviews
 */
@Entity
@Table(name = "review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {

  @Id
  @Column(name = "review_id")
  private UUID reviewId;

  @Version
  private Long version;

  @Column(nullable = false)
  private int rating;

  @Column(name = "comment", nullable = false)
  private String comment;

  @Column(name = "product_user_id", nullable = false)
  private UUID productUserId;

  @Column
  private Instant createdAt;

  @Column
  private Instant updatedAt;

  @Column
  private Instant deletedAt;

}