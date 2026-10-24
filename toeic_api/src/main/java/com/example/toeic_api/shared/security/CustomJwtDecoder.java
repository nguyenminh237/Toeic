package com.example.toeic_api.shared.security;

import com.example.toeic_api.modules.auth.service.TokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final TokenService tokens;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var signed = tokens.verify(token, false);
            var c = signed.getJWTClaimsSet();
            var claims = new HashMap<String, Object>(c.getClaims());
            claims.put("iat", c.getIssueTime().toInstant());
            claims.put("exp", c.getExpirationTime().toInstant());
            return new Jwt(
                    token,
                    c.getIssueTime().toInstant(),
                    c.getExpirationTime().toInstant(),
                    signed.getHeader().toJSONObject(),
                    claims);
        } catch (Exception e) {
            throw new BadJwtException("Invalid or revoked session");
        }
    }
}
