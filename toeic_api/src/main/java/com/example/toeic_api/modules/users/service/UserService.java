package com.example.toeic_api.modules.users.service;

import com.example.toeic_api.modules.users.dto.request.UserCreationRequest;
import com.example.toeic_api.modules.users.dto.response.UserResponse;
import com.example.toeic_api.modules.users.mapper.UserMapper;
import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.model.UserRole;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.modules.vocabulary.model.UserVocabularyCollection;
import com.example.toeic_api.modules.vocabulary.repository.UserVocabularyCollectionRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final UserVocabularyCollectionRepository collections;

    @Transactional
    public UserResponse createUser(UserCreationRequest r) {
        String username = r.getUsername().trim();
        String email = r.getEmail().trim().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(username)) throw new AppException(ErrorCode.USER_EXISTED);
        if (users.existsByEmail(email)) throw new AppException(ErrorCode.INVALID_EMAIL);
        User u = mapper.toUser(r);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(r.getPassword()));
        u.setRole(UserRole.USER);
        u.setEnabled(true);
        u.setAuthVersion(0L);
        users.saveAndFlush(u);
        var c =
                UserVocabularyCollection.builder()
                        .user(u)
                        .setName("Từ vựng của tôi")
                        .isDefault(true)
                        .build();
        collections.save(c);
        return mapper.toUserResponse(u);
    }

    public User requireUser(String ignored) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return users.findByUsername(auth.getName())
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }

    public boolean isAdmin() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        return a != null
                && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"));
    }

    public UserResponse me() {
        return mapper.toUserResponse(requireUser(null));
    }

    public UserResponse getUserById(String id) {
        var me = requireUser(null);
        if (!me.getId().equals(id) && !isAdmin()) throw new AppException(ErrorCode.UNAUTHORIZED);
        return mapper.toUserResponse(
                users.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse getUserByUsername(String name) {
        var me = requireUser(null);
        if (!me.getUsername().equals(name) && !isAdmin())
            throw new AppException(ErrorCode.UNAUTHORIZED);
        return mapper.toUserResponse(
                users.findByUsername(name)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public List<UserResponse> getAllUsers() {
        if (!isAdmin()) throw new AppException(ErrorCode.UNAUTHORIZED);
        return users.findAll().stream().map(mapper::toUserResponse).toList();
    }

    @Transactional
    public void changePassword(String current, String next) {
        var u = users.lockById(requireUser(null).getId()).orElseThrow();
        if (!encoder.matches(current, u.getPassword()))
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        u.setPassword(encoder.encode(next));
        u.setAuthVersion(u.getAuthVersion() + 1);
    }
}
