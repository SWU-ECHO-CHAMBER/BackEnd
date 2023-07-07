package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.auth.domain.LoginService;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@Setter
public class EmailLoginService extends LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private String email;

    @Autowired
    public EmailLoginService(JwtHandler jwtHandler, TokenService tokenService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(jwtHandler, tokenService);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 정보 조회
    @Override
    public void getUserData() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user == null)
            throw new Exception("Invalid email.");
        else
            super.setUser(user);
    }

    // 비밀번호 일치 여부 확인
    public void isPasswordMatch(String input_password) {
        if (passwordEncoder.matches(input_password, super.getUser().getEncoded_password()))
            throw new RuntimeException("Password does not match.");
    }
}
