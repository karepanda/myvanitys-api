package com.myvanitys.api.auth.infrastructure.persistence.repository;

import java.util.UUID;

import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

  UserEntity findByToken(String authorizationId);

}