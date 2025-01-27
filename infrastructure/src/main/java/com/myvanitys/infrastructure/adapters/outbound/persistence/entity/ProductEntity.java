package com.myvanitys.infrastructure.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, unique = true)
  private String Id;

 @ManyToOne
 @JoinColumn(name = "category_id")
 private CategoryEntity categoryEntity;

  @Column(length = 100)
  private String brand;

  @Column(nullable = false)
  private String name;

  @Column
  private int colorHex;

  @Column
  private Date createdAt;

  @Column
  private Date updatedAt;



}
