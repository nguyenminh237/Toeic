package com.example.toeic_api.modules.auth.controller;

import com.example.toeic_api.modules.auth.dto.request.AuthenticationRequest;
import com.example.toeic_api.modules.auth.dto.request.ForgotPasswordRequest;
import com.example.toeic_api.modules.auth.dto.request.IntrospectRequest;
import com.example.toeic_api.modules.auth.dto.request.LogoutRequest;
import com.example.toeic_api.modules.auth.dto.request.RefreshRequest;
import com.example.toeic_api.modules.auth.dto.request.ResetPasswordRequest;
import com.example.toeic_api.modules.auth.dto.response.AuthenticationResponse;
import com.example.toeic_api.modules.auth.dto.response.IntrospectResponse;
import com.example.toeic_api.modules.auth.service.AuthenticationService;
import com.example.toeic_api.shared.api.ApiResponse;
import com.nimbusds.jose.JOSEException;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refresh(request))
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request.getToken(), request);
        return ApiResponse.<Void>builder().build();
    }
}
