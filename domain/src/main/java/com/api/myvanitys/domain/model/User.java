package com.api.myvanitys.domain.model;

import com.api.myvanitys.domain.valueobject.EntityId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class User {
    private final EntityId id;
    private final String email;
    private final String name;

    public User(EntityId id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        User user = (User) object;

        return id.equals(user.id) &&
                email.equals(user.email) &&
                name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
