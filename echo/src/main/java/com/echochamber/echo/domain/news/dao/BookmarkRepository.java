package com.echochamber.echo.domain.news.dao;

import com.echochamber.echo.domain.model.BookmarkEntity;
import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {
    List<BookmarkEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);

    Boolean existsByUserAndNews(UserEntity user, NewsEntity news);
}
