package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "review_id")
  private UUID reviewId;

  @Column(nullable = false)
  private int rating;

  @Column(name = "comment", nullable = false)
  private String comment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_user_id", nullable = false)
  private ProductUserEntity productUserEntity;

  @Column
  private Instant createdAt;

  @Column
  private Instant updatedAt;

}