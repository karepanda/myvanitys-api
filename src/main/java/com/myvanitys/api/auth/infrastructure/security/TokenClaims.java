package com.myvanitys.api.auth.infrastructure.security;

public record TokenClaims(String userId, String email, String name) {

}
