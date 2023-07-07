package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.jwt.JwtHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@Setter
public class LeaveService {
    private final JwtHandler jwtHandler;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private UserEntity user;

    @Autowired
    public LeaveService(JwtHandler jwtHandler, TokenService tokenService, UserRepository userRepository) {
        this.jwtHandler = jwtHandler;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    // 유저 정보 삭제
    public void removeUser() {
        // Redis
        tokenService.removeRefresh(user);

        // DB
        userRepository.deleteById(user.getId());
    }
}
