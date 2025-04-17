package com.myvanitys.api.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

  @Nested
  class Constructor {

    @Test
    void when_allParametersAreValid_then_createUserSuccessfully() {
      final EntityId id = new EntityId(UUID.randomUUID());
      final String authorizationId = "auth123";
      final String email = "test@example.com";
      final String name = "Test User";

      final User user = new User(id, authorizationId, email, name);

      assertThat(user.getId()).isEqualTo(id);
      assertThat(user.getAuthorizationId()).isEqualTo(authorizationId);
      assertThat(user.getEmail()).isEqualTo(email);
      assertThat(user.getName()).isEqualTo(name);
    }

    @Test
    void when_authorizationIdIsNull_then_throwException() {
      final EntityId id = new EntityId(UUID.randomUUID());
      final String email = "test@example.com";
      final String name = "Test User";

      //noinspection DataFlowIssue
      assertThatThrownBy(() -> new User(id, null, email, name))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("authorizationId is marked non-null but is null");
    }

    @Test
    void when_emailIsNull_then_throwException() {
      final EntityId id = new EntityId(UUID.randomUUID());
      final String authorizationId = "auth123";
      final String name = "Test User";

      //noinspection DataFlowIssue
      assertThatThrownBy(() -> new User(id, authorizationId, null, name))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("email is marked non-null but is null");
    }

    @Test
    void when_nameIsNull_then_createUserSuccessfully() {
      final EntityId id = new EntityId(UUID.randomUUID());
      final String authorizationId = "auth123";
      final String email = "test@example.com";

      final User user = new User(id, authorizationId, email, null);

      assertThat(user.getName()).isNull();
    }
  }

  @Nested
  class EqualsAndHashCode {

    @Test
    void when_usersHaveSameId_then_areEqualAndHaveSameHashCode() {
      final UUID uuid = UUID.randomUUID();
      final EntityId id = new EntityId(uuid);

      final User user1 = new User(id, "auth1", "a@b.com", "Alice");
      final User user2 = new User(id, "auth2", "c@d.com", "Bob");

      assertThat(user1).satisfies(u -> {
        assertThat(u).isEqualTo(user2);
        assertThat(u).hasSameHashCodeAs(user2);
      });
    }

    @Test
    void when_usersHaveDifferentIds_then_notEqual() {
      final User user1 = new User(new EntityId(UUID.randomUUID()), "auth1", "a@b.com", "Alice");
      final User user2 = new User(new EntityId(UUID.randomUUID()), "auth2", "c@d.com", "Bob");

      assertThat(user1).isNotEqualTo(user2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void when_toStringCalled_then_containsAllFields() {
      final UUID uuid = UUID.randomUUID();
      final EntityId id = new EntityId(uuid);
      final String authorizationId = "authXYZ";
      final String email = "email@domain.com";
      final String name = "Johnny";

      final User user = new User(id, authorizationId, email, name);

      final String toString = user.toString();

      assertThat(toString)
          .contains(uuid.toString())
          .contains(authorizationId)
          .contains(email)
          .contains(name);
    }
  }
}
