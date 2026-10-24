package com.example.toeic_api.modules.auth.service;

import com.example.toeic_api.modules.auth.dto.request.AuthenticationRequest;
import com.example.toeic_api.modules.auth.dto.request.ForgotPasswordRequest;
import com.example.toeic_api.modules.auth.dto.request.IntrospectRequest;
import com.example.toeic_api.modules.auth.dto.request.LogoutRequest;
import com.example.toeic_api.modules.auth.dto.request.RefreshRequest;
import com.example.toeic_api.modules.auth.dto.request.ResetPasswordRequest;
import com.example.toeic_api.modules.auth.dto.response.AuthenticationResponse;
import com.example.toeic_api.modules.auth.dto.response.IntrospectResponse;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final PasswordResetService resets;

    public AuthenticationResponse authenticate(AuthenticationRequest r) {
        var u =
                users.findByUsername(r.getUsername())
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (!Boolean.TRUE.equals(u.getEnabled())
                || !encoder.matches(r.getPassword(), u.getPassword()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return AuthenticationResponse.builder().token(tokens.issue(u)).authenticated(true).build();
    }

    public IntrospectResponse introspect(IntrospectRequest r) {
        try {
            tokens.verify(r.getToken(), false);
            return IntrospectResponse.builder().valid(true).build();
        } catch (Exception e) {
            return IntrospectResponse.builder().valid(false).build();
        }
    }

    @Transactional
    public AuthenticationResponse refresh(RefreshRequest r) {
        var jwt = tokens.verify(r.getToken(), true);
        try {
            var u = users.findByUsername(jwt.getJWTClaimsSet().getSubject()).orElseThrow();
            users.lockById(u.getId()).orElseThrow();
            jwt = tokens.verify(r.getToken(), true);
            tokens.revoke(jwt);
            return AuthenticationResponse.builder()
                    .token(tokens.issue(u))
                    .authenticated(true)
                    .build();
        } catch (java.text.ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Transactional
    public void logout(LogoutRequest r) {
        try {
            tokens.revoke(tokens.verify(r.getToken(), true));
        } catch (AppException ignored) {
        }
    }

    public void forgotPassword(ForgotPasswordRequest r) {
        resets.request(r.getEmail());
    }

    public void resetPassword(String token, ResetPasswordRequest r) {
        resets.reset(token, r.getNewPassword());
    }
}
