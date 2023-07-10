package com.echochamber.echo.domain.news.dao;

import com.echochamber.echo.domain.model.BookmarkEntity;
import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {
    List<BookmarkEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);

    Optional<BookmarkEntity> findByUserAndNews(UserEntity user, NewsEntity news);

    Boolean existsByUserAndNews(UserEntity user, NewsEntity news);
}
