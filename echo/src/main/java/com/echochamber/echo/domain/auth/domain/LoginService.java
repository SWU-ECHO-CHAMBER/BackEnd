package com.echochamber.echo.domain.auth.domain;

import com.echochamber.echo.domain.auth.dao.RefreshTokenRepository;
import com.echochamber.echo.domain.model.RefreshTokenEntity;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@Service
public abstract class LoginService {
    private final JwtHandler jwtHandler;
    private final RefreshTokenRepository refreshTokenRepository;
    private UserEntity user;

    @Autowired
    public LoginService(JwtHandler jwtHandler, RefreshTokenRepository refreshTokenRepository) {
        this.jwtHandler = jwtHandler;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // 유저 정보 저장
    public abstract void getUserData() throws Exception;

    // 토큰 발행 및 저장
    public Map<String, String> generateTokens() {
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

    // Redis에 refreshToken 저장
    public void saveRefresh(String token) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(user.getId(), token);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    // 응답 전송
    public Map<String, Object> getResponse(Map<String, String> tokens) {
        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("userId", user.getId());
        responseData.putAll(tokens);

        return responseData;
    }

}
