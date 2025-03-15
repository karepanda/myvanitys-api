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
@Table(name = "user")
public class UserEntity {

  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, unique = true)
  private UUID userId;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(length = 200)
  private String email;

  @Column(length = 500)
  private String name;

  @Column(nullable = false)
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