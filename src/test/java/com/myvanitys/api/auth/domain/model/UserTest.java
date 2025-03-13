package com.myvanitys.api.auth.domain.model;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // ID's for testing with UUID
        EntityId userId1 = new EntityId(UUID.randomUUID());
        EntityId userId2 = new EntityId(UUID.randomUUID());

        // Instantiate users for testing
        user1 = new User(userId1, "auth123", "test1@example.com", "John Doe");
        user2 = new User(userId1, "auth123", "test1@example.com", "John Doe"); // same user as user1
        user3 = new User(userId2, "auth456", "test2@example.com", "Jane Doe"); // different user
    }

    @Test
    void testUserInitialization() {
        // verify that the user object is initialized correctly
        assertEquals("auth123", user1.getAuthorizationId(), "The authorizationId is incorrect");
        assertEquals("test1@example.com", user1.getEmail(), "The email is incorrect");
        assertEquals("John Doe", user1.getName(), "The name is incorrect");
    }

    @Test
    void testUsersWithSameIdAreEqual() {
        // Same IDs → Should be equal
        assertEquals(user1, user2, "Users with the same ID should be equal");
        assertEquals(user1.hashCode(), user2.hashCode(), "The hashCode should be the same for equal users");
    }

    @Test
    void testUsersWithDifferentIdAreNotEqual() {
        // Different IDs → Should not be equal
        assertNotEquals(user1, user3, "Users with different IDs should not be equal");
        assertNotEquals(user1.hashCode(), user3.hashCode(), "The hashCode should not be the same for different users");
    }

    @Test
    void testToStringFormat() {
        // Verify that the toString method returns the expected format
        String userToString = user1.toString();
        assertTrue(userToString.contains("authorizationId='auth123'"), "The toString does not contain the correct authorizationId");
        assertTrue(userToString.contains("email='test1@example.com'"), "The toString does not contain the correct email");
        assertTrue(userToString.contains("name='John Doe'"), "The toString does not contain the correct name");
    }

    @Test
    void testNonNullAuthorizationId() {
        Exception exception = assertThrows(NullPointerException.class, () -> new User(new EntityId(UUID.randomUUID()), null, "email@example.com", "Name"));
        assertEquals("authorizationId is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testNonNullEmail() {
        Exception exception = assertThrows(NullPointerException.class, () -> new User(new EntityId(UUID.randomUUID()), "authId", null, "Name"));
        assertEquals("email is marked non-null but is null", exception.getMessage());
    }

}