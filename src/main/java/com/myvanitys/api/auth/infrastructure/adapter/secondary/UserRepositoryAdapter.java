package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.port.secondary.UserRepository;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.common.LogSanitizer;
import com.myvanitys.api.common.valueobject.EntityId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Component
@AllArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @Override
  public Mono<User> save(User user) {
    log.debug("Saving user. userId={}, authorizationId={}, email={}",
        LogSanitizer.maskId(), LogSanitizer.maskId(), LogSanitizer.maskEmail(user.getEmail()));

    boolean isNew = user.getId() == null;

    if (isNew) {
      user = new User(
          EntityId.newId(),
          user.getAuthorizationId(),
          user.getEmail(),
          user.getName(),
          user.getCreateAt() != null ? user.getCreateAt() : Instant.now()
      );
      log.debug("Generated new ID for user: {}", LogSanitizer.maskId());
    }

    UserEntity userEntity = mapToEntity(user, isNew);
    log.debug("Mapped user entity. userId={}, authorizationId={}, email={}",
        LogSanitizer.maskId(),
        LogSanitizer.maskId(),
        LogSanitizer.maskEmail(userEntity.getEmail()));

    return Mono.fromCallable(() -> {
          UserEntity savedEntity = jpaUserRepository.save(userEntity);
          log.debug("User entity saved successfully. userId={}, authorizationId={}, email={}",
              LogSanitizer.maskId(),
              LogSanitizer.maskId(),
              LogSanitizer.maskEmail(savedEntity.getEmail()));
          return savedEntity;
        })
        .map(this::mapToDomain)
        .onErrorResume(e -> {
          log.error("Error saving user. type={}", e.getClass().getName());
          return Mono.error(e);
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<User> findByAuthorizationId(String authorizationId) {
    log.debug("Finding user by authorization ID: {}", LogSanitizer.maskId());

    return Mono.fromCallable(() -> jpaUserRepository.findByToken(authorizationId))
        .doOnNext(entity ->
          log.debug("Found user entity. userId={}, authorizationId={}, email={}",
              LogSanitizer.maskId(),
              LogSanitizer.maskId(),
              LogSanitizer.maskEmail(entity.getEmail())))
        .map(this::mapToDomain)
        .switchIfEmpty(Mono.defer(() -> {
          log.debug("No user found with authorization ID: {}", LogSanitizer.maskId());
          return Mono.empty();
        }))
        .subscribeOn(Schedulers.boundedElastic());
  }

  private UserEntity mapToEntity(User user, boolean isNew) {
    UserEntity entity = new UserEntity();

    entity.setUserId(user.getId().getValue());

    if (isNew) {
      entity.setCreatedAt(user.getCreateAt() != null ? user.getCreateAt() : Instant.now());
    } else {
      UserEntity existingEntity = jpaUserRepository.findById(user.getId().getValue()).orElse(null);
      if (existingEntity != null) {
        entity.setVersion(existingEntity.getVersion());
        entity.setCreatedAt(existingEntity.getCreatedAt());
        entity.setUpdatedAt(Instant.now());
      } else {
        log.warn("Attempting to update non-existent entity with ID: {}", LogSanitizer.maskId());
        entity.setCreatedAt(user.getCreateAt() != null ? user.getCreateAt() : Instant.now());
      }
    }

    entity.setToken(user.getAuthorizationId());
    entity.setEmail(user.getEmail());
    entity.setName(user.getName());

    return entity;
  }

  private User mapToDomain(UserEntity entity) {
    if (entity == null) {
      return null;
    }

    return new User(
        new EntityId(entity.getUserId()),
        entity.getToken(),
        entity.getEmail(),
        entity.getName(),
        entity.getCreatedAt()
    );
  }
}
