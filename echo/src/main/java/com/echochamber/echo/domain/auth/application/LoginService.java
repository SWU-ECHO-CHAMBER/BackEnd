package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.auth.domain.GoogleAuth;
import com.echochamber.echo.domain.model.UserEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@Slf4j
@Getter
@Setter
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuth googleAuth;

    @Autowired
    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, GoogleAuth googleAuth) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.googleAuth = googleAuth;
    }

    public UserEntity getUserData(String email, String password) throws Exception {
        // 유저 정보 조회
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user == null)
            throw new Exception("User not found.");

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getEncoded_password()))
            throw new RuntimeException("Password does not match.");

        return user;
    }

    // id-token 인증 및 정보 추출
    public UserEntity getUserData(String token) throws RuntimeException {
        try {
            // 인증 정보 추출
            UserEntity googleUser = googleAuth.authenticate(token);

            // 기존 유저인지 확인 (유저 아니라면 저장)
            UserEntity user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);
            if (user == null)
                user = userRepository.save(googleUser);

            return user;
        } catch (GeneralSecurityException | IOException e) {
            log.error(e.getMessage());

            throw new RuntimeException(e);
        }
    }
}
