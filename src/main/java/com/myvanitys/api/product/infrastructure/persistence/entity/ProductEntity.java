package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, unique = true)
  private UUID productId;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private CategoryEntity categoryEntity;

  @Column(length = 500)
  private String brand;

  @Column(nullable = false)
  private String name;

  @Column
  private String colorHex;

  @Column
  private Date createdAt;

  @Column
  private Date updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
    updatedAt = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = new Date();
  }

}
