package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.RefreshTokenRepository;
import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.RefreshTokenEntity;
import com.echochamber.echo.domain.model.UserEntity;
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

    public TokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtHandler jwtHandler) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtHandler = jwtHandler;
    }

    // token 발급
    public Map<String, String> generateTokens(UserEntity user) {
        // payloads 생성
        Map<String, Object> payloads = new LinkedHashMap<>();
        payloads.put("userId", user.getId());
        payloads.put("email", user.getEmail());

        // token 발행
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("accessToken", jwtHandler.generateToken(true, payloads));
        tokens.put("refreshToken", jwtHandler.generateToken(false, payloads));

        return tokens;
    }

    // 토큰 검증 및 정보 추출
    public UserEntity validateToken(String authorization) throws RuntimeException {
        String accessToken = jwtHandler.decodeBearer(authorization);
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
        String token = jwtHandler.decodeBearer(auth_refresh);
        RefreshTokenEntity refreshToken = refreshTokenRepository.findById(userId).orElse(null);

        if (refreshToken == null) {
            return false;
        }

        return refreshToken.getRefreshToken().equals(token);
    }

    // Redis에 refreshToken 저장
    public void saveRefresh(UserEntity user, String token) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(user.getId(), token);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    // redis 데이터 내에서 삭제
    public void removeRefresh(UserEntity user) {
        Long userId = user.getId();
        refreshTokenRepository.deleteByUserId(userId);
    }
}
