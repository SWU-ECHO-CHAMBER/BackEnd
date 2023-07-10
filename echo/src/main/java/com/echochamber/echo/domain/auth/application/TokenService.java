package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.RefreshTokenRepository;
import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.RefreshTokenEntity;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.TokenValidator;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@Service
public class TokenService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtHandler jwtHandler;
    private final TokenValidator tokenValidator;

    public TokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtHandler jwtHandler, TokenValidator tokenValidator) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtHandler = jwtHandler;
        this.tokenValidator = tokenValidator;
    }

    // token 발급
    public Map<String, String> generateTokens(UserEntity user) {
        // token 발행
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("access_token", jwtHandler.generateToken(true, user));
        tokens.put("refresh_token", jwtHandler.generateToken(false, user));

        return tokens;
    }

    // 토큰 검증 및 정보 추출
    public UserEntity validateToken(String authorization) throws RuntimeException {
        String accessToken = tokenValidator.decodeBearer(authorization);
        UserEntity user;
        Map<String, Object> payloads;

        try {
            payloads = jwtHandler.verifyJWT(accessToken);
        } catch (InvalidClaimException | ExpiredJwtException e) {
            throw new RuntimeException(e.getClass().getSimpleName());
        }

        user = userRepository.findByEmail(payloads.get("email").toString()).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found.");
        } else {
            return user;
        }
    }

    // Redis에 refreshToken 존재 여부 검사
    public boolean validateRefresh(Long userId, String auth_refresh) {
        String token = tokenValidator.decodeBearer(auth_refresh);
        RefreshTokenEntity refreshToken = refreshTokenRepository.findById(userId).orElse(null);

        if (refreshToken == null) {
            return false;
        }

        return refreshToken.getRefreshToken().equals(token);
    }

    // Redis에 refreshToken 저장
    public void saveRefresh(Long user_id, String token) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(user_id, token);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    // redis 데이터 내에서 삭제
    public void removeRefresh(Long user_id) throws RuntimeException {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(user_id).orElse(null);

        if (refreshTokenEntity == null)
            throw new RuntimeException("User not found.");

        refreshTokenRepository.delete(refreshTokenEntity);
    }
}
