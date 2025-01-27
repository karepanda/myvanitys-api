package com.myvanitys.infrastructure.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private String Id;

    @Column(nullable = false, unique = true)
    private String googleId;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private Date createdAt;

    @Column
    private Date updatedAt;
}
