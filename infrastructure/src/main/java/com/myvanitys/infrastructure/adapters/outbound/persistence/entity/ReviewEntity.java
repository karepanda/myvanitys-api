package com.myvanitys.infrastructure.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "review")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private String reviewId;

    @ManyToOne
    @JoinColumn(name = "user_Id")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "product_Id")
    private ProductEntity productEntity;

    @Column
    private int stars;

    @Column(length = 500)
    private String description;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;


}
