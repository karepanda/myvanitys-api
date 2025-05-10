package com.myvanitys.api.auth.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class User {

  private final EntityId id;

  private final String authorizationId;

  private final String email;

  private final String name;

  private final Instant createAt;

  public User(
      EntityId id,
      @NonNull String authorizationId,
      @NonNull String email,
      String name,
      @NonNull Instant createAt
  ) {
    this.id = id;
    this.authorizationId = Objects.requireNonNull(authorizationId, "authorizationId is marked non-null but is null");
    this.email = Objects.requireNonNull(email, "email is marked non-null but is null");
    this.name = name;
    this.createAt = Objects.requireNonNull(createAt, "registrationDate is marked non-null but is null");
  }
  
  public User(EntityId id, @NonNull String authorizationId, @NonNull String email, String name) {
    this(id, authorizationId, email, name, Instant.now());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    User user = (User) obj;
    return Objects.equals(id, user.id); // just compare the ID
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);  //Only id
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", authorizationId='" + authorizationId + '\'' +
        ", email='" + email + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
