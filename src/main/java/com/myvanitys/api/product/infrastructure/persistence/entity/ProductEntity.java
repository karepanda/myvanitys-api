package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for products
 */
@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

  @Id
  @Column(name = "product_id")
  private UUID productId;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private CategoryEntity category;

  @Version
  private Long version;

  @Column(nullable = false)
  private String name;

  @Column
  private String brand;

  @Column(name = "color_hex")
  private String colorHex;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

}