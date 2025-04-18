package com.myvanitys.api.auth.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "\"user\"")
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
  private Instant createdAt;

  @Column
  private Instant updatedAt;


}