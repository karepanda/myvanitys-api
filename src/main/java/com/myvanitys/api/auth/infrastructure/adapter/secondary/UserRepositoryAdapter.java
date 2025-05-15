package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import java.time.Instant;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.port.UserRepository;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@AllArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @Override
  public Mono<User> save(User user) {
    log.debug("Saving user: {}", user);

    // Check if this is a new user or an existing one
    boolean isNew = user.getId() == null;

    if (isNew) {
      // For new users, ensure they have an ID from the domain
      user = new User(
          EntityId.newId(),
          user.getAuthorizationId(),
          user.getEmail(),
          user.getName(),
          user.getCreateAt() != null ? user.getCreateAt() : Instant.now()
      );
      log.debug("Generated new ID for user: {}", user.getId());
    }

    // Convert User (domain model) to UserEntity (JPA entity)
    UserEntity userEntity = mapToEntity(user, isNew);
    log.debug("Mapped to entity: {}", userEntity);

    // Save the entity and map it back to the domain model
    return Mono.fromCallable(() -> {
          UserEntity savedEntity = jpaUserRepository.save(userEntity);
          log.debug("Entity saved successfully: {}", savedEntity);
          return savedEntity;
        })
        .map(this::mapToDomain)  // Aquí usamos .map porque mapToDomain devuelve User
        .onErrorResume(e -> {
          log.error("Error saving user: {}", e.getMessage(), e);
          return Mono.error(e);
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<User> findByAuthorizationId(String authorizationId) {
    log.debug("Finding user by authorization ID: {}", authorizationId);

    return Mono.fromCallable(() -> jpaUserRepository.findByToken(authorizationId))
        .flatMap(entity -> {
          if (entity == null) {
            log.debug("No user found with authorization ID: {}", authorizationId);
            return Mono.empty();
          }
          log.debug("Found user entity: {}", entity);
          return Mono.just(mapToDomain(entity));  // Convertir a Mono.just aquí
        })
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
        log.warn("Attempting to update non-existent entity with ID: {}", user.getId().getValue());
        entity.setCreatedAt(user.getCreateAt() != null ? user.getCreateAt() : Instant.now());
      }
    }

    entity.setToken(user.getAuthorizationId());
    entity.setEmail(user.getEmail());
    entity.setName(user.getName());

    return entity;
  }

  // Vuelve a ser un método síncrono
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