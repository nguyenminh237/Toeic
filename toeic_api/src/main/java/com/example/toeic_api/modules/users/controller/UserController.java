package com.example.toeic_api.modules.users.controller;

import com.example.toeic_api.modules.users.dto.request.PasswordChangeRequest;
import com.example.toeic_api.modules.users.dto.request.UserCreationRequest;
import com.example.toeic_api.modules.users.dto.response.UserResponse;
import com.example.toeic_api.modules.users.service.UserService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder().result(userService.createUser(request)).build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder().result(userService.getAllUsers()).build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder().result(userService.getUserById(userId)).build();
    }

    @GetMapping("/username/{username}")
    ApiResponse<UserResponse> getUserByUsername(@PathVariable String username) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserByUsername(username))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.<UserResponse>builder().result(userService.me()).build();
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> password(@RequestBody @Valid PasswordChangeRequest r) {
        userService.changePassword(r.currentPassword(), r.newPassword());
        return ApiResponse.<Void>builder().build();
    }
}
