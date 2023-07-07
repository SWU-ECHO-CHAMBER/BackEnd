package com.echochamber.echo.domain.auth.api;

import com.echochamber.echo.domain.auth.application.*;
import com.echochamber.echo.domain.auth.dto.EmailJoinDto;
import com.echochamber.echo.domain.auth.dto.JoinResponseDto;
import com.echochamber.echo.domain.model.UserEntity;
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
    private final LogoutService logoutService;
    private final LeaveService leaveService;
    private final TokenService tokenService;

    @Autowired
    public AuthApi(SocialLoginService socialLoginService, EmailLoginService emailLoginService, EmailJoinService emailJoinService, LogoutService logoutService, LeaveService leaveService, TokenService tokenService) {
        this.socialLoginService = socialLoginService;
        this.emailLoginService = emailLoginService;
        this.emailJoinService = emailJoinService;
        this.logoutService = logoutService;
        this.leaveService = leaveService;
        this.tokenService = tokenService;
    }

    // 소셜 로그인
    @PostMapping("/social")
    public ResponseEntity<ResponseDto> authSocial(@RequestHeader("id-token") String id_token) {
        Map<String, Object> responseData;
        try {
            // 1. id-token 인증 및 정보 추출
            socialLoginService.setToken(id_token);
            socialLoginService.getUserData();

            // 2. 토큰 발행 및 redis 저장
            Map<String, String> tokens = socialLoginService.generateTokens();

            // 3. response
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
        try {
            // 1. email 존재 여부 검사
            emailLoginService.setEmail(email);
            emailLoginService.getUserData();

            // 2. 비밀번호 일치 여부 확인
            emailLoginService.isPasswordMatch(password);

            // 3. 토큰 발행 및 redis 저장
            Map<String, String> tokens = tokenService.generateTokens(emailLoginService.getUser());
            tokenService.saveRefresh(emailLoginService.getUser(), tokens.get("refreshToken"));

            // 4. response
            JoinResponseDto dto = new JoinResponseDto(emailLoginService.getUser().getId(), tokens);

            return ResponseEntity.ok(DataResponseDto.of(dto, 200));
        } catch (Exception e) {
            if (e.getMessage().equals("Password does not match.")) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Password does not match."));
            }

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
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto> logout(@RequestHeader(value = "Authorization") String auth) {
        try {
            // token 검증
            UserEntity user = tokenService.validateToken(auth);
            logoutService.setUser(user);

            // 캐시 데이터 내 유저 정보 삭제
            logoutService.removeUser();

            // 응답
            return ResponseEntity.ok(ResponseDto.of(200));
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("Invalid accessToken.")) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, e.getMessage()));
            }

            if (e.getMessage().equals("User not found.")) {
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));
            }

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }

    // 회원탈퇴
    @PostMapping("/leave")
    public ResponseEntity<ResponseDto> leave(@RequestHeader(value = "Authorization") String auth) {
        try {
            // 토큰 검증
            // token 검증
            UserEntity user = tokenService.validateToken(auth);
            leaveService.setUser(user);

            // 캐시 데이터 / 데이터베이스 내 유저 정보 삭제
            leaveService.removeUser();

            // 응답
            return ResponseEntity.ok(ResponseDto.of(200));
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("ExpiredJwtException")) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, e.getMessage()));
            }

            if (e.getMessage().equals("User not found.")) {
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));
            }

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }

    // 토큰 재발급
    public ResponseEntity<ResponseDto> refreshToken(@RequestHeader(value = "Authorization") String auth, @RequestHeader(value = "Refresh") String auth_refresh) {
        UserEntity user;
        // accessToken 검증
        try {
            tokenService.validateToken(auth);

            return ResponseEntity.status(403).body(ResponseDto.of(403, "AccessToken is still valid."));
        } catch (RuntimeException e) {
            if (!e.getMessage().equals("ExpiredJwtException")) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Invalid accessToken."));
            }
        }

        // refreshToken 검증
        try {
            user = tokenService.validateToken(auth_refresh);

            if (!tokenService.validateRefresh(user.getId(), auth_refresh)) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Invalid refreshToken."));
            }
        } catch (RuntimeException e) {
            if (e.getMessage().equals("ExpiredJwtException")) {
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Expired refreshToken."));
            }

            return ResponseEntity.status(401).body(ResponseDto.of(401, "Invalid refreshToken."));
        }

        // 토큰 재발급
        try {
            // 이전 RefreshToken 삭제
            tokenService.removeRefresh(user);

            // 토큰 발행 및 redis 저장
            Map<String, String> tokens = tokenService.generateTokens(emailLoginService.getUser());
            tokenService.saveRefresh(emailLoginService.getUser(), tokens.get("refreshToken"));

            // response
            return ResponseEntity.ok(DataResponseDto.of(tokens, 200));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, "Server error."));
        }
    }
}
