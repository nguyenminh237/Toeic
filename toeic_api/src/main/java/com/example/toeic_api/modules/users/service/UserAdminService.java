package com.example.toeic_api.modules.users.service;

import com.example.toeic_api.modules.users.dto.response.UserResponse;
import com.example.toeic_api.modules.users.mapper.UserMapper;
import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.model.UserRole;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminService {
    private final UserRepository users;
    private final UserMapper mapper;

    public List<UserResponse> searchUsers(String q) {
        return (q == null || q.isBlank() ? users.findAll() : users.search(q.trim()))
                .stream().map(mapper::toUserResponse).toList();
    }

    public UserResponse setUserEnabled(String id, boolean enabled) {
        var u = user(id);
        if (u.getEnabled() != enabled) {
            u.setEnabled(enabled);
            u.setAuthVersion(u.getAuthVersion() + 1);
        }
        return mapper.toUserResponse(u);
    }

    public UserResponse setUserRole(String id, String role) {
        UserRole parsed;
        try {
            parsed = UserRole.valueOf(role.toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_CONTENT);
        }
        var u = user(id);
        if (u.getRole() != parsed) {
            u.setRole(parsed);
            u.setAuthVersion(u.getAuthVersion() + 1);
        }
        return mapper.toUserResponse(u);
    }

    private User user(String id) {
        return users.lockById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
