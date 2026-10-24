package com.example.toeic_api.modules.admin.controller;

import com.example.toeic_api.modules.admin.service.AdminOverviewService;
import com.example.toeic_api.shared.api.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    AdminOverviewService adminService;

    @GetMapping
    ApiResponse<java.util.Map<String, Object>> getDashboard() {
        return ApiResponse.<java.util.Map<String, Object>>builder()
                .result(adminService.getDashboard())
                .build();
    }
}
