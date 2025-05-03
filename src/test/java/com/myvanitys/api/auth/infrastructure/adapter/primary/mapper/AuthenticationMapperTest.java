package com.myvanitys.api.auth.infrastructure.adapter.primary.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.model.v1.AuthResponse;
import com.myvanitys.api.model.v1.GoogleAuthRequest;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

class AuthenticationMapperTest {
    private final AuthenticationMapper mapper = Mappers.getMapper(AuthenticationMapper.class);

    @Test
    void toCommand_whenRequestIsNull_shouldReturnNull() {
        // When
        GoogleAuthCommand result = mapper.toCommand(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toCommand_whenCodeIsNull_shouldReturnNull() {
        // Given
        GoogleAuthRequest request = new GoogleAuthRequest();

        // When
        GoogleAuthCommand result = mapper.toCommand(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toCommand_whenCodeIsProvided_shouldReturnCommand() {
        // Given
        String code = "test-code";
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setCode(code);

        // When
        GoogleAuthCommand result = mapper.toCommand(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(code);
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Given
        String token = "test-token";
        String authorizationId = "test-user-id";
        String email = "test@example.com";
        String name = "Test User";

        final EntityId userId = new EntityId(UUID.randomUUID());

        User user = new User(userId, authorizationId, email, name);

        UserSession session = UserSession.create(token, user);

        // When
        AuthResponse response = mapper.toResponse(session);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getUserId()).isEqualTo(userId.getValue());
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);
    }
}