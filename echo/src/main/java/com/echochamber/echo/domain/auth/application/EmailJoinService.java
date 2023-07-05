package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.StringValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailJoinService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public EmailJoinService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // 이메일 값 유효성 확인
    public boolean isEmailValid(String email) {
        return StringValidator.isValidEmail(email);
    }

    // 이메일 중복확인
    public boolean checkEmailDup(String email) {
        return userRepository.existsByEmail(email);
    }

    // 닉네임, 비밀번호 값 유효성 확인
    public Map<String, Boolean> validateValues(String nickname, String password) {
        Map<String, Boolean> validation = new HashMap<>();
        validation.put("nickname", StringValidator.isValidNickname(nickname));
        validation.put("password", StringValidator.isValidPassword(password));

        return validation;
    }

    // 비밀번호 암호화
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    // DB 정보 저장
    public Long saveData(String email, String nickname, String encoded_password) {
        UserEntity newUser = new UserEntity(email, nickname, encoded_password);

        return userRepository.save(newUser).getId();
    }
}
