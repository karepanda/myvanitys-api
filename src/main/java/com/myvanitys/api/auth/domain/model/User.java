package com.myvanitys.api.auth.domain.model;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.NonNull;

public class User {
    private final EntityId id;
    private final String authorizationId;
    private final String email;
    private final String name;

    public User(EntityId id, @NonNull String authorizationId, @NonNull String email, String name) {
        this.id = id;
        this.authorizationId = authorizationId;
        this.email = email;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, authorizationId, email, name);
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
