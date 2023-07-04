package com.echochamber.echo.global.util.newsdata;

import com.echochamber.echo.domain.model.NewsEntity;
import com.echochamber.echo.domain.news.dao.NewsRepository;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.Source;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.request.SourcesRequest;
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.kwabenaberko.newsapilib.models.response.SourcesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class NewsProvider {
    private final String API_KEY = System.getenv("NEWS_API_KEY");
    private final NewsApiClient newsApiClient = new NewsApiClient(API_KEY);
    private final CrawlingProvider crawlingProvider;
    private final NewsRepository newsRepository;

    @Autowired
    public NewsProvider(CrawlingProvider crawlingProvider, NewsRepository newsRepository) {
        this.crawlingProvider = crawlingProvider;
        this.newsRepository = newsRepository;
    }

    public String getContent(String url, String query) throws InternalError {
        String content = null;

        try {
            content = crawlingProvider.getNewsContent(url, query);
        } catch (IOException e) {
            throw new InternalError("Crawling error.");
        }

        return content;
    }

    public String getQuery(String source) {

        return source.equals("CNN") ? ".article__content .paragraph" : "#article-body p,h2";
    }

    public void saveData(Article article) {
        // Check if article already exists
        if (newsRepository.existsByUrl(article.getUrl())) {
            return;
        }

        String content = null;

        try {
            content = crawlingProvider.getNewsContent(article.getUrl(), getQuery(article.getSource().getName()));
        } catch (IOException e) {
            throw new InternalError("Crawling error.");
        }

        if (content != null) {
            NewsEntity news = new NewsEntity(article, content, null);
            newsRepository.save(news);
        }
    }

    public void getEverything() throws InternalError {
        log.info("News data update in progress.");
        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .language("en")
                        .domains("cnn.com, time.com")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        // db 저장
                        for (Article article : response.getArticles()) {
                            saveData(article);
                        }

                        log.info("News data update completed.");

                        getTopHeadlines();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error(throwable.getMessage());
                        throw new InternalError("News API request error.");
                    }
                }
        );
    }

    public void getTopHeadlines() throws InternalError {
        log.info("Top Headlines update in progress.");
        newsApiClient.getTopHeadlines(
                new TopHeadlinesRequest.Builder()
                        .language("en")
                        .sources("cnn,time")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) throws InternalError {
                        for (int i = 0; i < 5; i++) {
                            Article article = response.getArticles().get(i);
                            Optional<NewsEntity> news = newsRepository.findByUrl(article.getUrl());

                            try {
                                if (news.isPresent()) {
                                    newsRepository.updateIsTop(true, news.get().getId());
                                } else {
                                    newsRepository.save(
                                            new NewsEntity(
                                                    article,
                                                    getContent(article.getUrl(), getQuery(article.getSource().getName())),
                                                    null,
                                                    true
                                            )
                                    );
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage());
                                throw new InternalError("Database access error.");
                            }
                        }

                        log.info("Top Headlines update completed.");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error(throwable.getMessage());
                        throw new InternalError("News API request error.");
                    }
                }
        );
    }

    public void getSources() {
        newsApiClient.getSources(
                new SourcesRequest.Builder()
                        .language("en")
                        .country("us")
                        .build(),
                new NewsApiClient.SourcesCallback() {
                    @Override
                    public void onSuccess(SourcesResponse response) {
                        StringBuilder res = new StringBuilder("sources\n");
                        for (Source result : response.getSources()) {
                            String value = "{ " + result.getName() + ", " + result.getUrl() + ", " + result.getId() + " }";
                            res.append(value).append("\n");
                        }
                        log.info(String.valueOf(res));
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error(throwable.getMessage());
                    }
                }
        );
    }
}
