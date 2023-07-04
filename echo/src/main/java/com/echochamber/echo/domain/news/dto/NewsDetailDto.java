package com.echochamber.echo.domain.news.dto;

import com.echochamber.echo.domain.model.NewsEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class NewsDetailDto {
    private Long news_id;
    private String title;
    private String content;
    private String source;
    private LocalDateTime published_at;
    private String author;
    private String image_url;

    public NewsDetailDto(NewsEntity news) {
        this.news_id = news.getId();
        this.title = news.getTitle();
        this.content = news.getContent();
        this.source = news.getSource();
        this.published_at = news.getPublishedAt();
        this.author = news.getAuthor();
        this.image_url = news.getImage_url();
    }
}
