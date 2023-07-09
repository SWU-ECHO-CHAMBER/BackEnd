package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@Setter
public class LogoutService {
    private final JwtHandler jwtHandler;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public LogoutService(JwtHandler jwtHandler, UserRepository userRepository, TokenService tokenService) {
        this.jwtHandler = jwtHandler;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    // Redis 내 refreshToken 삭제
    public void removeUser(UserEntity user) {
        tokenService.removeRefresh(user.getId());
    }
}
