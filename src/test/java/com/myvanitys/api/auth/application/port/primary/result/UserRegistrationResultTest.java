package com.myvanitys.api.auth.application.port.primary.result;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserRegistrationResultTest {

    @Test
    void shouldCreateUserRegistrationResultSuccessfully() {
        //Arrange
        UUID uuid = UUID.randomUUID();
        EntityId userId = new EntityId(uuid);
        User user = new User(
                userId,
                "AutorizationId",
                "Useremail@gamil.com",
                "UserName"
        );

        UserSession session = new UserSession(
                "userId123",
                user
        );

        //Act
        UserRegistrationResult result = new UserRegistrationResult(session);

        //Assert
        assertNotNull(result);
        assertEquals(session, result.session());
    }

    @Test
    void shouldThrowExceptionWhenSessionIsNull(){
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new UserRegistrationResult(null));
    }


}