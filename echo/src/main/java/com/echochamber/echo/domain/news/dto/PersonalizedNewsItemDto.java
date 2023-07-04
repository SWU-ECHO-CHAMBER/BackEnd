package com.echochamber.echo.domain.news.dto;

import com.echochamber.echo.domain.model.NewsEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PersonalizedNewsItemDto {
    private Long news_id;
    private String title;
    private String source;
    private LocalDateTime published_at;
    private String author;
    private String image_url;
    private Map<String, Object> preference_info;

    public PersonalizedNewsItemDto(NewsEntity news, Map<String, Object> preference_info) {
        this.news_id = news.getId();
        this.title = news.getTitle();
        this.source = news.getSource();
        this.published_at = news.getPublishedAt();
        this.author = news.getAuthor();
        this.image_url = news.getImage_url();
        this.preference_info = preference_info;
    }
}
