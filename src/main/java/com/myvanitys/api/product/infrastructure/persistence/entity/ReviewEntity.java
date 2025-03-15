package com.myvanitys.api.product.infrastructure.persistence.entity;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review")
public class ReviewEntity {

  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, unique = true)
  private UUID reviewId;

  @ManyToOne
  @JoinColumn(name = "product_user_id")
  private ProductUserEntity productUserEntity;

  @Column
  private int rating;

  @Column(length = 500)
  private String description;

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

