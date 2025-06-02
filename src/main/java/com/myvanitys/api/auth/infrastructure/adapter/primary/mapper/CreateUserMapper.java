package com.myvanitys.api.auth.infrastructure.adapter.primary.mapper;

import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.model.v1.CreateUserRequest;
import com.myvanitys.api.model.v1.UserCreatedResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValueCheckStrategy = org.mapstruct.NullValueCheckStrategy.ALWAYS)
public interface CreateUserMapper {
    default RegisterUserCommand toCommand(CreateUserRequest request) {
        if(request == null || request.getAuthProvider() == null || request.getAuthCode() == null) {
            return null;
        }

        return RegisterUserCommand.of(
            request.getAuthProvider().toString(), 
            request.getAuthCode());
    }

    @Mapping(source = "id.value", target = "userId")
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    UserCreatedResponse toResponse(User user);
}