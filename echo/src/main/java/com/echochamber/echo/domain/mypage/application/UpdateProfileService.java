package com.echochamber.echo.domain.mypage.application;

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
public class UpdateProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageHandler imageHandler;

    @Autowired
    public UpdateProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder, ImageHandler imageHandler) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageHandler = imageHandler;
    }

    // 닉네임 변경
    public void updateNickname(UserEntity user, String new_nickname) throws RuntimeException {
        // 유효성 검사
        if (!StringValidator.isValidNickname(new_nickname))
            throw new RuntimeException("Invalid nickname format.");

        // 이전 닉네임과 같은지 검사
        if (user.getNickname().equals(new_nickname))
            throw new RuntimeException("Nickname unchanged.");

        // 데이터 업데이트
        userRepository.updateNickname(new_nickname, user.getId());
    }

    // 비밀번호 변경
    public void updatePassword(UserEntity user, String new_password) throws RuntimeException {
        // 유효성 검사
        if (!StringValidator.isValidPassword(new_password))
            throw new RuntimeException("Invalid password format.");

        // 비밀번호 암호화
        String encoded = passwordEncoder.encode(new_password);

        // 데이터 업데이트
        userRepository.updatePassword(encoded, user.getId());
    }

    // 프로필 이미지 변경
    public String updateProfileImage(UserEntity user, MultipartFile image) throws IOException, RuntimeException {
        // 이미지 삭제
        if (user.getProfileImagePath() != null) {
            imageHandler.deleteProfileImage(user.getProfileImagePath());
        }

        // 이미지 저장 (비어있는 image일 경우 이미지 삭제)
        String file_path = image == null || image.isEmpty() ? null : imageHandler.saveProfileImage(user.getEmail(), image);

        // 데이터 업데이트
        userRepository.updateProfileImagePath(file_path, user.getId());
        
        return file_path;
    }
}
