package com.myvanitys.api.auth.infrastructure.adapter.primary;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.RegisterUserUseCase;
import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.domain.exception.UserRegistrationFailedException;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.AuthenticationMapper;
import com.myvanitys.api.auth.infrastructure.adapter.primary.mapper.CreateUserMapper;
import com.myvanitys.api.model.v1.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthControllerTest {

  private static final UUID REQUEST_ID = UUID.fromString("11111111-0000-0000-0000-000000000001");
  private static final UUID FLOW_ID = UUID.fromString("11111111-0000-0000-0000-000000000002");
  private static final String AUTH_CODE = "4/0AbCd_ExAmPlE-CoD3";

  private GoogleAuthenticationUseCase googleAuthenticationUseCase;
  private AuthenticationMapper authenticationMapper;
  private CreateUserMapper createUserMapper;
  private RegisterUserUseCase registerUserUseCase;
  private AuthController controller;

  @BeforeEach
  void setUp() {
    googleAuthenticationUseCase = mock(GoogleAuthenticationUseCase.class);
    authenticationMapper = mock(AuthenticationMapper.class);
    createUserMapper = mock(CreateUserMapper.class);
    registerUserUseCase = mock(RegisterUserUseCase.class);
    controller = new AuthController(
        googleAuthenticationUseCase,
        authenticationMapper,
        createUserMapper,
        registerUserUseCase);
  }

  @Test
  void createUser_whenRegistrationResultIsNull_thenThrowsUserRegistrationFailedException() {
    // Given
    CreateUserRequest request = new CreateUserRequest()
        .authProvider(CreateUserRequest.AuthProviderEnum.GOOGLE)
        .authCode(AUTH_CODE);
    RegisterUserCommand command = RegisterUserCommand.of("GOOGLE", AUTH_CODE);

    when(createUserMapper.toCommand(request)).thenReturn(command);
    // Mono.empty().block() returns null, which reaches the intended null-result branch
    when(registerUserUseCase.execute(command, REQUEST_ID, FLOW_ID)).thenReturn(Mono.empty());

    // When / Then
    UserRegistrationFailedException exception = assertThrows(
        UserRegistrationFailedException.class,
        () -> controller.createUser(REQUEST_ID, FLOW_ID, request));

    assertEquals("User registration failed", exception.getMessage());

    // And - the exact request is mapped to the exact command
    ArgumentCaptor<CreateUserRequest> requestCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);
    verify(createUserMapper).toCommand(requestCaptor.capture());
    assertSame(request, requestCaptor.getValue());

    // And - the use case receives the exact mapped command, request UUID and flow UUID
    ArgumentCaptor<RegisterUserCommand> commandCaptor =
        ArgumentCaptor.forClass(RegisterUserCommand.class);
    verify(registerUserUseCase).execute(commandCaptor.capture(), eq(REQUEST_ID), eq(FLOW_ID));
    assertSame(command, commandCaptor.getValue());

    // And - the success path is not taken and Google authentication is untouched
    verify(createUserMapper, never()).toResponse(any());
    verifyNoInteractions(googleAuthenticationUseCase, authenticationMapper);
  }
}
