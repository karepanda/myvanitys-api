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

  @Column(nullable = false)
  private String name;

  @Column(length = 500)
  private String brand;

 @ManyToOne
 @JoinColumn(name = "category_id")
 private CategoryEntity categoryEntity;

  @Column
  private int colorHex;

  @Column
  private Date createdAt;

  @Column
  private Date updatedAt;



}
