package com.example.toeic_api.modules.users.controller;

import com.example.toeic_api.modules.users.dto.request.UserRoleRequest;
import com.example.toeic_api.modules.users.dto.request.UserStatusRequest;
import com.example.toeic_api.modules.users.dto.response.UserResponse;
import com.example.toeic_api.modules.users.service.UserAdminService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    UserAdminService adminService;

    @GetMapping
    ApiResponse<List<UserResponse>> searchUsers(@RequestParam(required = false) String q) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(adminService.searchUsers(q))
                .build();
    }

    @PatchMapping("/{userId}/status")
    ApiResponse<UserResponse> setStatus(
            @PathVariable String userId, @RequestBody @Valid UserStatusRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(adminService.setUserEnabled(userId, request.getEnabled()))
                .build();
    }

    @PatchMapping("/{userId}/role")
    ApiResponse<UserResponse> setRole(
            @PathVariable String userId, @RequestBody @Valid UserRoleRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(adminService.setUserRole(userId, request.getRole()))
                .build();
    }
}
