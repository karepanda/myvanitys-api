package com.myvanitys.api.auth.infrastructure.adapter.primary.mapper;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.model.v1.AuthResponse;
import com.myvanitys.api.model.v1.GoogleAuthRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AuthenticationMapper {

  default GoogleAuthCommand toCommand(GoogleAuthRequest request) {
    if (request == null || request.getCode() == null) {
      return null;
    }
    
    return GoogleAuthCommand.of(request.getCode());
  }

  @Mapping(source = "token", target = "token")
  @Mapping(source = "user.id.value", target = "userId")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.name", target = "name")
  AuthResponse toResponse(UserSession session);
}