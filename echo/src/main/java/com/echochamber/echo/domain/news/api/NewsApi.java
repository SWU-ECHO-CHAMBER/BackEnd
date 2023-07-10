package com.echochamber.echo.domain.news.api;

import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.domain.news.application.BookmarkService;
import com.echochamber.echo.domain.news.application.NewsService;
import com.echochamber.echo.domain.news.dto.BookmarkDto;
import com.echochamber.echo.domain.news.dto.NewsDetailDto;
import com.echochamber.echo.domain.news.dto.NewsItemDto;
import com.echochamber.echo.domain.news.dto.TopicResponseDto;
import com.echochamber.echo.global.common.response.DataResponseDto;
import com.echochamber.echo.global.common.response.ResponseDto;
import com.echochamber.echo.global.util.TokenValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/news")
public class NewsApi {
    private final TokenValidator tokenValidator;
    private final NewsService newsService;
    private final BookmarkService bookmarkService;

    @Autowired
    public NewsApi(TokenValidator tokenValidator, NewsService newsService, BookmarkService bookmarkService) {
        this.tokenValidator = tokenValidator;
        this.newsService = newsService;
        this.bookmarkService = bookmarkService;
    }

    // Get entire list of news data
    @GetMapping()
    public ResponseEntity<ResponseDto> getAll() {
        List<NewsItemDto> data;
        try {
            data = newsService.getAllService();

            if (data.size() == 0) {
                log.error("News data update error.");
                return ResponseEntity.status(404).body(ResponseDto.of(404, "Data not found."));
            }

            return ResponseEntity.ok(DataResponseDto.of(data, 200));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.internalServerError().body(ResponseDto.of(500));
        }
    }

    // Get list of topic words // * Get list of personalized news data (incomplete)
    @GetMapping(value = "/opp/{news_id}")
    public ResponseEntity<ResponseDto> getOppNews(@PathVariable Long news_id) throws JsonProcessingException {
        TopicResponseDto data = null;
        try {
            data = newsService.getOppNewsService(news_id);

            if (data == null) {
                return ResponseEntity.badRequest().body(ResponseDto.of(400, "Invalid news id."));
            }

            return ResponseEntity.ok(DataResponseDto.of(data, 200));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.internalServerError().body(ResponseDto.of(500));
        }
    }

    // Get detail data of article
    @GetMapping("/detail/{news_id}")
    public ResponseEntity<ResponseDto> getDetail(@RequestHeader(value = "Authorization") String auth, @PathVariable Long news_id) {
        try {
            UserEntity user = tokenValidator.validateToken(auth);
            NewsDetailDto data = newsService.getDetail(user, news_id);
            
            return ResponseEntity.ok(DataResponseDto.of(data, 200));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Invalid token."));

            if (e.getMessage().equals("User not found."))
                return ResponseEntity.status(401).body(ResponseDto.of(401, e.getMessage()));

            if (e.getMessage().equals("Invalid news-id."))
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

    // Get top headline
    @GetMapping("/top")
    public ResponseEntity<ResponseDto> getTopHeadline() {
        try {
            NewsItemDto data = newsService.getTopHeadlineService();

            if (data == null) {
                return ResponseEntity.status(404).body(ResponseDto.of(404, "Data not found."));
            }

            return ResponseEntity.ok(DataResponseDto.of(data, 200));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseDto.of(500));
        }
    }

    // 북마크 추가 및 삭제
    @PostMapping("/mark/{news-id}")
    public ResponseEntity<ResponseDto> setBookmark(@RequestHeader(value = "Authorization") String auth, @PathVariable(value = "news-id") Long news_id) {
        try {
            UserEntity user = tokenValidator.validateToken(auth);
            boolean isMarked = bookmarkService.setBookmark(user, news_id);
            BookmarkDto dto = new BookmarkDto(isMarked);

            return ResponseEntity.ok(DataResponseDto.of(dto, 200));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("InvalidClaimException") || e.getMessage().equals("ExpiredJwtException"))
                return ResponseEntity.status(401).body(ResponseDto.of(401, "Invalid token."));

            if (e.getMessage().equals("User not found."))
                return ResponseEntity.status(401).body(ResponseDto.of(401, e.getMessage()));

            if (e.getMessage().equals("Invalid news-id."))
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
}
