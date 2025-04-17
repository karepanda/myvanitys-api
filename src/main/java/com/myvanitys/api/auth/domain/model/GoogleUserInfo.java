package com.myvanitys.api.auth.domain.model;

import java.util.Objects;

public record GoogleUserInfo(
    String id,
    String email,
    String name,
    String pictureUrl
) {
    public GoogleUserInfo{
        Objects.requireNonNull(id, "Google ID cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

        if(!isValidEmail(email)){
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

}
