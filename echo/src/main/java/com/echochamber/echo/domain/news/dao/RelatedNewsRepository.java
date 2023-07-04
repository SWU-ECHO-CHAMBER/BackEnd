package com.echochamber.echo.domain.news.dao;

import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.model.RelatedNewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatedNewsRepository extends JpaRepository<RelatedNewsEntity, Long> {
    List<RelatedNewsEntity> findAllByNews(NewsEntity news);
}
