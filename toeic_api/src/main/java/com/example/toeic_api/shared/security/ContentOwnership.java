package com.example.toeic_api.shared.security;

import com.example.toeic_api.modules.users.service.UserService;
import com.example.toeic_api.shared.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentOwnership {
    private final UserService users;

    public String currentId() {
        return users.requireUser(null).getId();
    }

    public boolean owns(String ownerId) {
        return ownerId != null && ownerId.equals(currentId());
    }

    public void require(String ownerId) {
        if (!owns(ownerId)) throw new AppException(ErrorCode.CONTENT_NOT_FOUND);
    }
}
