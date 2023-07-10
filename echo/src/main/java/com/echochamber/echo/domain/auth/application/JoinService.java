package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.ImageHandler;
import com.echochamber.echo.global.util.StringValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class JoinService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ImageHandler imageHandler;

    @Autowired
    public JoinService(PasswordEncoder passwordEncoder, UserRepository userRepository, ImageHandler imageHandler) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.imageHandler = imageHandler;
    }

    // 비밀번호 암호화
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    // 이메일 값 유효성 확인 및 중복확인
    public boolean checkEmailDup(String email) throws RuntimeException {
        if (!StringValidator.isValidEmail(email))
            throw new RuntimeException("Invalid email format.");

        return userRepository.existsByEmail(email);
    }

    // 닉네임, 비밀번호 값 유효성 확인
    public void validateValues(String nickname, String password) throws RuntimeException {
        if (!StringValidator.isValidNickname(nickname))
            throw new RuntimeException("Invalid nickname format.");

        if (!StringValidator.isValidPassword(password))
            throw new RuntimeException("Invalid password format.");
    }

    // DB 정보 저장
    public Long saveData(String email, String nickname, String password, MultipartFile image) throws IOException, IllegalArgumentException {
        // 비밀번호 암호화
        String encoded_password = encodePassword(password);

        // 프로필 이미지 저장
        String profile_path = image == null || image.isEmpty() ? null : imageHandler.saveProfileImage(email, image);

        // 유저 객체 생성
        UserEntity newUser = new UserEntity(email, nickname, encoded_password, null, profile_path);

        return userRepository.save(newUser).getId();
    }
}
