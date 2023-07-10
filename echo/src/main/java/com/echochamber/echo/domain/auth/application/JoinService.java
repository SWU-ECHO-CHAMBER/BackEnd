package com.echochamber.echo.domain.auth.application;

import com.echochamber.echo.domain.auth.dao.UserRepository;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.util.StringValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class JoinService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final String DIR_PATH;

    @Autowired
    public JoinService(PasswordEncoder passwordEncoder, UserRepository userRepository, @Value("${PROFILE_DATABASE_URL}") String path) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.DIR_PATH = path;
    }

    // 비밀번호 암호화
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    // 프로필 이미지 저장
    private String saveProfileImage(String email, MultipartFile image) throws IOException {
        String file_name = email + ".jpeg";
        String file_path = DIR_PATH + file_name;
        image.transferTo(new File(file_path));

        return file_path;
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
    public Long saveData(String email, String nickname, String password, MultipartFile image) throws IOException {
        String encoded_password = encodePassword(password);
        String profile_path = image == null || image.isEmpty() ? null : saveProfileImage(email, image);
        UserEntity newUser = new UserEntity(email, nickname, encoded_password, null, profile_path);

        return userRepository.save(newUser).getId();
    }
}
