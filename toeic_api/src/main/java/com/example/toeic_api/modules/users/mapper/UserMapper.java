package com.example.toeic_api.modules.users.mapper;

import com.example.toeic_api.modules.users.dto.request.UserCreationRequest;
import com.example.toeic_api.modules.users.dto.response.UserResponse;
import com.example.toeic_api.modules.users.model.User;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
}
