package com.echochamber.echo.global.util;

import com.echochamber.echo.domain.auth.dao.RefreshTokenRepository;
import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.RefreshTokenEntity;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class TokenValidator {
    private final JwtHandler jwtHandler;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public TokenValidator(JwtHandler jwtHandler, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtHandler = jwtHandler;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    // Bearer 해석
    public String decodeBearer(String str) {
        return Arrays.stream(str.split("Bearer ")).toList().get(1);
    }

    public UserEntity validateToken(String authorization) throws RuntimeException {
        // bearer 해석
        String accessToken = decodeBearer(authorization);
        Map<String, Object> payloads;

        // access token 검증 및 유저 정보 추출
        try {
            payloads = jwtHandler.verifyJWT(accessToken);
        } catch (InvalidClaimException | ExpiredJwtException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getClass().getSimpleName());
        }

        // user 정보 존재 여부 검사
        UserEntity user = userRepository.findByEmail((String) payloads.get("email")).orElse(null);
        if (user == null)
            throw new RuntimeException("Invalid token.");

        // refresh token 존재 여부 검사
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(Long.valueOf((Integer) payloads.get("userId"))).orElse(null);
        if (refreshTokenEntity == null)
            throw new RuntimeException("Unauthorized user.");

        return user;
    }
}
