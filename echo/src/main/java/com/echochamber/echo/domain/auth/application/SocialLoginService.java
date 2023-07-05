package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.RefreshTokenRepository;
import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.auth.domain.GoogleAuth;
import com.echochamber.echo.domain.auth.domain.LoginService;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Getter
@Setter
@Service
@Slf4j
public class SocialLoginService extends LoginService {
    private final UserRepository userRepository;
    private final GoogleAuth googleAuth;
    private String token;

    @Autowired
    public SocialLoginService(JwtHandler jwtHandler, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, GoogleAuth googleAuth) {
        super(jwtHandler, refreshTokenRepository);
        this.userRepository = userRepository;
        this.googleAuth = googleAuth;
    }

    // id-token 인증 및 정보 추출
    @Override
    public void getUserData() throws RuntimeException {
        try {
            // 인증 정보 추출
            UserEntity googleUser = googleAuth.authenticate(token);

            // 기존 유저인지 확인 (유저 아니라면 저장)
            UserEntity user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);
            if (user == null)
                user = userRepository.save(googleUser);

            super.setUser(user);
        } catch (GeneralSecurityException | IOException e) {
            log.error(e.getMessage());

            throw new RuntimeException(e);
        }
    }
}
