package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.port.UserRepository;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@AllArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  /**
   * Saves a domain {@link User} by converting it to a JPA entity and persisting it.
   *
   * @param user the domain user to save
   * @return a {@link Mono} emitting the saved domain user
   */
  @Override
  public Mono<User> save(User user) {
    // Convert User (domain model) to UserEntity (JPA entity)
    UserEntity userEntity = mapToEntity(user);

    // Save the entity and map it back to the domain model
    return Mono.fromCallable(() -> jpaUserRepository.save(userEntity))
        .map(this::mapToDomain)
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * Finds a domain {@link User} by authorization ID (mapped to token in the entity).
   *
   * @param authorizationId the authorization ID used to find the user
   * @return a {@link Mono} emitting the domain user if found
   */
  @Override
  public Mono<User> findByAuthorizationId(String authorizationId) {
    // In your entity there is no authorizationId, but rather token, so we adjust accordingly
    return Mono.fromCallable(() -> jpaUserRepository.findByToken(authorizationId))
        .map(this::mapToDomain)
        .subscribeOn(Schedulers.boundedElastic());
  }

  private UserEntity mapToEntity(User user) {
    UserEntity entity = new UserEntity();
    if (user.getId() != null) {
      entity.setUserId(user.getId().getValue());
    }
    entity.setEmail(user.getEmail());
    entity.setName(user.getName());
    // We use token instead of authorizationId
    entity.setToken(user.getAuthorizationId());
    // There is no pictureUrl in the entity
    // We don't need to set createdAt or updatedAt, they are handled via @PrePersist/@PreUpdate
    return entity;
  }

  private User mapToDomain(UserEntity entity) {
    return new User(new EntityId(entity.getUserId()), entity.getEmail(), entity.getName(), entity.getToken());
  }
}

