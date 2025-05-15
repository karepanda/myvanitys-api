package com.myvanitys.api.auth.infrastructure.adapter.primary.mapper;

import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.model.v1.CreateUserRequest;
import com.myvanitys.api.model.v1.UserCreatedResponse;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CreateUserMapperTest {

    private CreateUserMapper createUserMapper;

    @BeforeEach
    void setUp() {
        createUserMapper = Mappers.getMapper(CreateUserMapper.class);
    }

    @Nested
    class ToCommand{

        @Test
        void when_validRequest_then_returnsCommand() {
// Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setAuthProvider(CreateUserRequest.AuthProviderEnum.GOOGLE);
            request.setAuthCode("valid_code");

            // Act
            RegisterUserCommand command = createUserMapper.toCommand(request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.provider()).isEqualTo("google");
            assertThat(command.code()).isEqualTo("valid_code");
        }
    }

    @Test
    void when_validUser_then_returnsResponse() {
        // Given
        User user = new User(EntityId.newId(),"Authorization_id", "test@example.com", "Test User", Instant.now());

        // When
        UserCreatedResponse result = createUserMapper.toResponse(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(user.getId().getValue());
        assertThat(result.getToken()).isNull();
        assertThat(result.getExpiresIn()).isNull();
        assertThat(result.getRefreshToken()).isNull();
    }



}