package com.echochamber.echo.global.util.newsdata;

import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.news.dao.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class DataUpdate {
    private final NewsProvider newsProvider;
    private final NewsRepository newsRepository;

    @Autowired
    public DataUpdate(NewsProvider newsProvider, NewsRepository newsRepository) {
        this.newsProvider = newsProvider;
        this.newsRepository = newsRepository;
    }

    // Update every 18hrs
    @Scheduled(fixedRate = 18 * 60 * 60 * 1000)
    public void UpdateNewsData() {
        // Check latest update
        NewsEntity latestData = newsRepository.findTopByOrderByPublishedAtDesc().orElse(null);
        boolean isUpdatable = latestData != null && LocalDateTime.now(ZoneId.of("EST")).isAfter(latestData.getCreatedAt().plusHours(18));

        // Update data
        if (latestData == null || isUpdatable) {
            newsProvider.getEverything();
        } else {
            // If data already up-to-date, get top headlines only
            newsProvider.getTopHeadlines();
        }
    }

    // Update top headlines every 1hrs
    @Scheduled(cron = "0 0 */1 * * *")
    public void UpdateTopHeadlines() {
        newsProvider.getTopHeadlines();
    }
}
