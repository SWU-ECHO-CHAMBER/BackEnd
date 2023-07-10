package com.echochamber.echo.domain.mypage.api;

import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.domain.mypage.application.UpdateProfileService;
import com.echochamber.echo.domain.mypage.dto.ProfileImageDto;
import com.echochamber.echo.domain.news.application.BookmarkService;
import com.echochamber.echo.domain.news.dto.NewsItemDto;
import com.echochamber.echo.global.common.response.DataResponseDto;
import com.echochamber.echo.global.common.response.ResponseDto;
import com.echochamber.echo.global.util.TokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mypage")
public class MyPageApi {
    private final UpdateProfileService updateProfileService;
    private final BookmarkService bookmarkService;
    private final TokenValidator tokenValidator;

    @Autowired
    public MyPageApi(UpdateProfileService updateProfileService, BookmarkService bookmarkService, TokenValidator tokenValidator) {
        this.updateProfileService = updateProfileService;
        this.bookmarkService = bookmarkService;
        this.tokenValidator = tokenValidator;
    }

    // 닉네임 변경
    @PatchMapping("/nickname")
    public ResponseEntity<ResponseDto> updateNickname(@RequestHeader(value = "Authorization") String auth, @RequestParam(value = "nickname") String nickname) {
        try {
            // 토큰 검사
            UserEntity user = tokenValidator.validateToken(auth);

            // 닉네임 변경
            updateProfileService.updateNickname(user, nickname);

            // 응답
            return ResponseEntity.status(201).body(ResponseDto.of(201));
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(403).body(ResponseDto.of(403, "Token expired."));

            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("Invalid token."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid token."));

            if (e.getMessage().equals("Unauthorized user."))
                return ResponseEntity.status(403).body(ResponseDto.of(403, e.getMessage()));

            if (e.getMessage().equals("Invalid nickname format.") || e.getMessage().equals("Nickname unchanged."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<ResponseDto> updatePassword(@RequestHeader(value = "Authorization") String auth, @RequestParam(value = "password") String password) {
        try {
            // 토큰 검사
            UserEntity user = tokenValidator.validateToken(auth);

            // 비밀번호 변경
            updateProfileService.updatePassword(user, password);

            // 응답
            return ResponseEntity.status(201).body(ResponseDto.of(201));
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(403).body(ResponseDto.of(403, "Token expired."));

            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("Invalid token."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid token."));

            if (e.getMessage().equals("Unauthorized user."))
                return ResponseEntity.status(403).body(ResponseDto.of(403, e.getMessage()));

            if (e.getMessage().equals("Invalid password format."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 프로필 이미지 변경
    @PatchMapping("/profile-image")
    public ResponseEntity<ResponseDto> updateProfileImage(@RequestHeader(value = "Authorization") String auth, @RequestPart(value = "profile", required = false) MultipartFile profile) {
        try {
            // 토큰 검사
            UserEntity user = tokenValidator.validateToken(auth);

            // 프로필 이미지 변경
            String file_path = updateProfileService.updateProfileImage(user, profile);

            // 응답
            if (file_path == null) {
                // 프로필 이미지 삭제한 경우
                return ResponseEntity.status(201).body(ResponseDto.of(201, "Profile image deleted."));
            } else {
                // 프로필 이미지 삽입 / 변경한 경우
                ProfileImageDto dto = new ProfileImageDto(file_path);
                return ResponseEntity.status(201).body(DataResponseDto.of(dto, 201));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseDto.of(400, e.getMessage()));
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(403).body(ResponseDto.of(403, "Token expired."));

            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("Invalid token."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid token."));

            if (e.getMessage().equals("Unauthorized user."))
                return ResponseEntity.status(403).body(ResponseDto.of(403, e.getMessage()));

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }

    // 북마크 목록 조회
    @GetMapping("/mark")
    public ResponseEntity<ResponseDto> getBookmarkList(@RequestHeader(value = "Authorization") String auth) {
        try {
            // 토큰 검사
            UserEntity user = tokenValidator.validateToken(auth);

            // 북마크 리스크 조회
            List<NewsItemDto> result = bookmarkService.getBookmarkList(user);

            // 응답
            if (result.size() == 0) {
                // 북마크 조회 결과가 없는 경우
                return ResponseEntity.status(200).body(ResponseDto.of(200, "No result."));
            } else {
                // 북마크 조회 결과가 있는 경우
                return ResponseEntity.status(200).body(DataResponseDto.of(result, 200));
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(403).body(ResponseDto.of(403, "Token expired."));

            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("Invalid token."))
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid token."));

            if (e.getMessage().equals("Unauthorized user."))
                return ResponseEntity.status(403).body(ResponseDto.of(403, e.getMessage()));

            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(ResponseDto.of(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        }
    }
}
