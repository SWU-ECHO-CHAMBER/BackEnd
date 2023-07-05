package com.echochamber.echo.domain.auth.api;

import com.echochamber.echo.domain.auth.application.EmailJoinService;
import com.echochamber.echo.domain.auth.application.EmailLoginService;
import com.echochamber.echo.domain.auth.application.SocialLoginService;
import com.echochamber.echo.domain.auth.dto.EmailJoinDto;
import com.echochamber.echo.global.common.response.DataResponseDto;
import com.echochamber.echo.global.common.response.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthApi {
    private final SocialLoginService socialLoginService;
    private final EmailLoginService emailLoginService;
    private final EmailJoinService emailJoinService;

    @Autowired
    public AuthApi(SocialLoginService socialLoginService, EmailLoginService emailLoginService, EmailJoinService emailJoinService) {
        this.socialLoginService = socialLoginService;
        this.emailLoginService = emailLoginService;
        this.emailJoinService = emailJoinService;
    }

    // 소셜 로그인
    @PostMapping("/social")
    public ResponseEntity<ResponseDto> authSocial(@RequestHeader("id-token") String id_token) {
        Map<String, Object> responseData;
        try {
            // 1. id-token 인증 및 정보 추출
            socialLoginService.setToken(id_token);
            socialLoginService.getUserData();

            // 2. 토큰 발행
            Map<String, String> tokens = socialLoginService.generateTokens();

            // 3. redis에 refreshToken 저장
            socialLoginService.saveRefresh(tokens.get("refreshToken"));

            // 4. response
            responseData = socialLoginService.getResponse(tokens);

            return ResponseEntity.ok(DataResponseDto.of(responseData, 200));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid id token."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }

    // 이메일 로그인
    @PostMapping("/email")
    public ResponseEntity<ResponseDto> authEmail(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password) {
        Map<String, Object> responseData;
        try {
            // 1. email 존재 여부 검사
            emailLoginService.setInput_email(email);
            emailLoginService.getUserData();

            // 2. 비밀번호 일치 여부 확인
            emailLoginService.setInput_password(password);
            if (!emailLoginService.isPasswordMatch()) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Password does not match."));
            }

            // 3. 토큰 발행
            Map<String, String> tokens = emailLoginService.generateTokens();

            // 4. redis에 refreshToken 저장
            emailLoginService.saveRefresh(tokens.get("refreshToken"));

            // 5. response
            responseData = socialLoginService.getResponse(tokens);

            return ResponseEntity.ok(DataResponseDto.of(responseData, 200));
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid email.")) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Email does not exist."));
            }

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }

    // 이메일 중복확인
    @PostMapping("/email/check")
    public ResponseEntity<ResponseDto> checkEmail(@RequestParam(value = "email") String email) {
        log.info(email);
        try {
            if (!emailJoinService.isEmailValid(email)) {
                return ResponseEntity.status(400).body(ResponseDto.of(400, "Invalid email format."));
            }

            if (emailJoinService.checkEmailDup(email)) {
                return ResponseEntity.status(400).body(ResponseDto.of(400, "Email already exists."));
            }

            return ResponseEntity.ok(ResponseDto.of(200));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }

    // 회원가입
    @PostMapping("/email/join")
    public ResponseEntity<ResponseDto> joinEmail(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password, @RequestParam(value = "nickname") String nickname) {
        try {
            if (!emailJoinService.isEmailValid(email)) {
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid email format."));
            }

            if (emailJoinService.checkEmailDup(email)) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Email already exists."));
            }

            // nickname, password 유효성 검사
            Map<String, Boolean> values = emailJoinService.validateValues(nickname, password);
            if (!values.get("nickname")) {
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid nickname format."));
            } else if (!values.get("password")) {
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid password format."));
            }

            // DB 저장
            Long new_user_id = emailJoinService.saveData(email, nickname, emailJoinService.encodePassword(password));
            EmailJoinDto data = new EmailJoinDto(new_user_id);

            // 응답
            return ResponseEntity.ok(DataResponseDto.of(data, 200));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }

    // 로그아웃
    // 회원탈퇴
    // 토큰 재발급
}
