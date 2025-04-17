package com.myvanitys.api.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleUserInfoTest {
    @Test
    void shouldCreateGoogleUserInfoSuccessfully() {
        GoogleUserInfo userInfo = new GoogleUserInfo(
                "123456",
                "user@example.com",
                "Test User",
                "https://example.com/picture.jpg"
        );

        assertEquals("123456", userInfo.id());
        assertEquals("user@example.com", userInfo.email());
        assertEquals("Test User", userInfo.name());
        assertEquals("https://example.com/picture.jpg", userInfo.pictureUrl());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> new GoogleUserInfo(
                null,
                "user@example.com",
                "Test User",
                "https://example.com/picture.jpg"
        ));

        assertEquals("Google ID cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> new GoogleUserInfo(
                "123456",
                null,
                "Test User",
                "https://example.com/picture.jpg"
        ));

        assertEquals("Email cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new GoogleUserInfo(
                "123456",
                "invalid-email",
                "Test User",
                "https://example.com/picture.jpg"
        ));

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> new GoogleUserInfo(
                "123456",
                "user@example.com",
                null,
                "https://example.com/picture.jpg"
        ));

        assertEquals("Name cannot be null", exception.getMessage());
    }

}