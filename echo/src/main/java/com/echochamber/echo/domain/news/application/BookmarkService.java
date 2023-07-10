package com.echochamber.echo.domain.news.application;

import com.echochamber.echo.domain.model.BookmarkEntity;
import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.domain.news.dao.BookmarkRepository;
import com.echochamber.echo.domain.news.dao.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final NewsRepository newsRepository;

    @Autowired
    public BookmarkService(BookmarkRepository bookmarkRepository, NewsRepository newsRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.newsRepository = newsRepository;
    }

    // 북마크 설정
    public boolean setBookmark(UserEntity user, Long news_id) throws RuntimeException {
        // news 조회
        NewsEntity news = newsRepository.findById(news_id).orElse(null);
        if (news == null)
            throw new RuntimeException("Invalid news-id.");

        // 북마크 데이터 조회
        BookmarkEntity bookmarkEntity = bookmarkRepository.findByUserAndNews(user, news).orElse(null);
        if (bookmarkEntity == null) {
            // 북마크 데이터가 존재하지 않을 경우 (새로 등록)
            bookmarkEntity = new BookmarkEntity(user, news);
            bookmarkRepository.save(bookmarkEntity);
            return true;
        } else {
            // 북마크 데이터가 존재할 경우 (등록 취소)
            bookmarkRepository.delete(bookmarkEntity);
            return false;
        }
    }
}
