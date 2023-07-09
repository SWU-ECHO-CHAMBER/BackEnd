package com.echochamber.echo.domain.auth.api;

import com.echochamber.echo.domain.auth.application.*;
import com.echochamber.echo.domain.auth.dto.EmailJoinDto;
import com.echochamber.echo.domain.auth.dto.LoginResponseDto;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.global.common.response.DataResponseDto;
import com.echochamber.echo.global.common.response.ResponseDto;
import com.echochamber.echo.global.util.TokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthApi {
    private final LoginService loginService;
    private final JoinService joinService;
    private final LogoutService logoutService;
    private final LeaveService leaveService;
    private final TokenService tokenService;
    private final TokenValidator tokenValidator;

    @Autowired
    public AuthApi(LoginService loginService, JoinService joinService, LogoutService logoutService, LeaveService leaveService, TokenService tokenService, TokenValidator tokenValidator) {
        this.loginService = loginService;
        this.joinService = joinService;
        this.logoutService = logoutService;
        this.leaveService = leaveService;
        this.tokenService = tokenService;
        this.tokenValidator = tokenValidator;
    }

    // 소셜 로그인
    @PostMapping("/social")
    public ResponseEntity<ResponseDto> authSocial(@RequestHeader("id-token") String id_token) {
        try {
            // 1. id-token 인증 및 정보 추출
            UserEntity user = loginService.getUserData(id_token);

            // 2. 토큰 발행 및 redis 저장
            Map<String, String> tokens = tokenService.generateTokens(user);

            // 3. response
            LoginResponseDto dto = new LoginResponseDto(user.getId(), tokens);

            return ResponseEntity.ok(DataResponseDto.of(dto, 200));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid id token."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 이메일 로그인
    @PostMapping("/email")
    public ResponseEntity<ResponseDto> authEmail(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password) {
        try {
            // 1. 인증 및 유저 정보 추출
            UserEntity user = loginService.getUserData(email, password);

            // 2. 토큰 발행 및 redis 저장
            Map<String, String> tokens = tokenService.generateTokens(user);
            tokenService.saveRefresh(user.getId(), tokens.get("refreshToken"));

            // 3. response
            LoginResponseDto dto = new LoginResponseDto(user.getId(), tokens);

            return ResponseEntity.ok(DataResponseDto.of(dto, 200));
        } catch (Exception e) {
            if (e.getMessage().equals("Password does not match."))
                return ResponseEntity.status(403).body(ResponseDto.of(403, e.getMessage()));

            if (e.getMessage().equals("Invalid email."))
                return ResponseEntity.status(401).body(ResponseDto.of(401, e.getMessage()));

            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 이메일 중복확인
    @PostMapping("/email/check")
    public ResponseEntity<ResponseDto> checkEmail(@RequestParam(value = "email") String email) {
        try {
            if (joinService.checkEmailDup(email)) {
                return ResponseEntity.status(400).body(ResponseDto.of(400, "Email already exists."));
            }

            return ResponseEntity.ok(ResponseDto.of(200));
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid email format."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 회원가입
    @PostMapping("/email/join")
    public ResponseEntity<ResponseDto> joinEmail(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password, @RequestParam(value = "nickname") String nickname) {
        try {
            // email 유효성 검사
            if (joinService.checkEmailDup(email)) {
                return ResponseEntity.status(400).body(ResponseDto.of(400, "Email already exists."));
            }

            // nickname, password 유효성 검사
            joinService.validateValues(nickname, password);

            // 비밀번호 암호화 및 DB 저장
            Long new_user_id = joinService.saveData(email, nickname, password);
            EmailJoinDto data = new EmailJoinDto(new_user_id);

            // 응답
            return ResponseEntity.ok(DataResponseDto.of(data, 200));
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid email format.") || e.getMessage().equals("Invalid nickname format.") || e.getMessage().equals("Invalid password format."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto> logout(@RequestHeader(value = "Authorization") String auth) {
        try {
            // token 검증
            UserEntity user = tokenValidator.validateToken(auth);

            // 캐시 데이터 내 유저 정보 삭제
            logoutService.removeUser(user);

            // 응답
            return ResponseEntity.ok(ResponseDto.of(200));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Invalid token."));

            if (e.getMessage().equals("User not found."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));

            if (e.getMessage().equals("Unauthorized user."))
                return ResponseEntity.status(403).body(ResponseDto.of(403, e.getMessage()));

            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 회원탈퇴
    @PostMapping("/leave")
    public ResponseEntity<ResponseDto> leave(@RequestHeader(value = "Authorization") String auth) {
        try {
            // token 검증
            UserEntity user = tokenValidator.validateToken(auth);

            // 캐시 데이터 / 데이터베이스 내 유저 정보 삭제
            leaveService.removeUser(user);

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

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 토큰 재발급
    @GetMapping("/refresh")
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
            tokenService.removeRefresh(user.getId());

            // 토큰 발행 및 redis 저장
            Map<String, String> tokens = tokenService.generateTokens(user);
            tokenService.saveRefresh(user.getId(), tokens.get("refreshToken"));

            // response
            return ResponseEntity.ok(DataResponseDto.of(tokens, 200));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }
}
