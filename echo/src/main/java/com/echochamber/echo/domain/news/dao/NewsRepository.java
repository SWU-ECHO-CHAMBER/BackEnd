package com.echochamber.echo.domain.news.dao;

import com.echochamber.echo.domain.model.NewsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {
    boolean existsByUrl(String url);

    List<NewsEntity> findAllByOrderByPublishedAtDesc();

    Optional<NewsEntity> findByUrl(String url);

    Optional<NewsEntity> findTopByIsTopTrueOrderByPublishedAtDesc();

    Optional<NewsEntity> findTopByOrderByPublishedAtDesc();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE NewsEntity n SET n.isTop = :isTop WHERE n.id = :id")
    void updateIsTop(@Param("isTop") boolean isTop, @Param("id") Long id);
}
