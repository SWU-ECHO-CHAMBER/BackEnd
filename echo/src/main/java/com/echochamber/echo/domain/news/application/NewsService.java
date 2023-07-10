package com.echochamber.echo.domain.news.application;

import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.model.UserEntity;
import com.echochamber.echo.domain.news.dao.BookmarkRepository;
import com.echochamber.echo.domain.news.dao.NewsRepository;
import com.echochamber.echo.domain.news.dto.NewsDetailDto;
import com.echochamber.echo.domain.news.dto.NewsItemDto;
import com.echochamber.echo.domain.news.dto.TopicResponseDto;
import com.echochamber.echo.global.util.newsdata.TopicProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NewsService {
    private final NewsRepository newsRepository;
    private final BookmarkRepository bookmarkRepository;
    private final TopicProvider topicProvider;

    @Autowired
    public NewsService(NewsRepository newsRepository, BookmarkRepository bookmarkRepository, TopicProvider topicProvider) {
        this.newsRepository = newsRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.topicProvider = topicProvider;
    }

    // Get entire list of news data
    public List<NewsItemDto> getAllService() throws Exception {
        List<NewsItemDto> result = new ArrayList<>();
        List<NewsEntity> articles = newsRepository.findAllByOrderByPublishedAtDesc();

        for (NewsEntity news : articles) {
            NewsItemDto newsItemDto = new NewsItemDto(news);

            result.add(newsItemDto);
        }

        return result;
    }

    // Get list of personalized news data
    public TopicResponseDto getOppNewsService(Long news_id) throws Exception {
        NewsEntity newsEntity = newsRepository.findById(news_id).orElse(null);

        if (newsEntity == null) {
            throw new Exception("Invalid news id.");
        }

        TopicResponseDto topicResponseDto = null;
        String content = newsEntity.getContent();

        if (content != null) {
            topicResponseDto = topicProvider.getTopicWords(content);
        }

        return topicResponseDto;
    }

    // Get detail data of article
    public NewsDetailDto getDetail(UserEntity user, Long news_id) throws RuntimeException {
        NewsEntity news = newsRepository.findById(news_id).orElse(null);
        if (news == null)
            throw new RuntimeException("Invalid news-id.");

        boolean isMarked = bookmarkRepository.existsByUserAndNews(user, news);
        
        return new NewsDetailDto(news, isMarked);
    }

    // Get top headline
    public NewsItemDto getTopHeadlineService() throws InternalError {
        NewsEntity headline = newsRepository.findTopByIsTopTrueOrderByPublishedAtDesc().orElse(null);

        if (headline == null) {
            return null;
        }

        return new NewsItemDto(headline);
    }
}
