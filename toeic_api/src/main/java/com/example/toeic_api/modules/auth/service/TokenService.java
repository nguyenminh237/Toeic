package com.example.toeic_api.modules.auth.service;

import com.example.toeic_api.modules.auth.model.InvalidatedToken;
import com.example.toeic_api.modules.auth.repository.InvalidatedTokenRepository;
import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserRepository users;
    private final InvalidatedTokenRepository invalidated;
    private final Clock clock;

    @Value("${jwt.signer-key}")
    private String secret;

    @Value("${jwt.valid-duration:1}")
    private long validHours;

    @Value("${jwt.refreshable-duration:24}")
    private long refreshHours;

    @jakarta.annotation.PostConstruct
    void validateConfiguration() {
        if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 64
                || validHours < 1
                || refreshHours < validHours)
            throw new IllegalStateException(
                    "JWT_SIGNER_KEY must contain at least 64 bytes; refresh duration must cover the"
                            + " access duration");
    }

    public String issue(User u) {
        try {
            Instant now = clock.instant();
            var claims =
                    new JWTClaimsSet.Builder()
                            .subject(u.getUsername())
                            .issuer("toeicup")
                            .issueTime(Date.from(now))
                            .expirationTime(
                                    Date.from(
                                            now.plus(
                                                    validHours,
                                                    java.time.temporal.ChronoUnit.HOURS)))
                            .jwtID(UUID.randomUUID().toString())
                            .claim("scope", u.getRole().name())
                            .claim("authVersion", u.getAuthVersion())
                            .build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            jwt.sign(new MACSigner(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot issue token", e);
        }
    }

    public SignedJWT verify(String token, boolean refresh) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWTClaimsSet c = jwt.getJWTClaimsSet();
            if (!JWSAlgorithm.HS512.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(
                            new MACVerifier(
                                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    || !"toeicup".equals(c.getIssuer())
                    || c.getIssueTime() == null
                    || c.getExpirationTime() == null
                    || c.getJWTID() == null) throw new Exception();
            Instant expiry =
                    refresh
                            ? c.getIssueTime()
                                    .toInstant()
                                    .plus(refreshHours, java.time.temporal.ChronoUnit.HOURS)
                            : c.getExpirationTime().toInstant();
            if (!clock.instant().isBefore(expiry) || invalidated.existsById(c.getJWTID()))
                throw new Exception();
            User u = users.findByUsername(c.getSubject()).orElseThrow();
            if (!Boolean.TRUE.equals(u.getEnabled())
                    || !Objects.equals(c.getLongClaim("authVersion"), u.getAuthVersion())
                    || !u.getRole().name().equals(c.getStringClaim("scope"))) throw new Exception();
            return jwt;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void revoke(SignedJWT jwt) {
        try {
            var c = jwt.getJWTClaimsSet();
            Instant refreshEnd =
                    c.getIssueTime()
                            .toInstant()
                            .plus(refreshHours, java.time.temporal.ChronoUnit.HOURS);
            Instant expiry =
                    c.getExpirationTime().toInstant().isAfter(refreshEnd)
                            ? c.getExpirationTime().toInstant()
                            : refreshEnd;
            invalidated.save(
                    InvalidatedToken.builder()
                            .id(c.getJWTID())
                            .expiryTime(Date.from(expiry))
                            .build());
        } catch (java.text.ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
