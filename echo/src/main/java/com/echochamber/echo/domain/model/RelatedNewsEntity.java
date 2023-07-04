package com.echochamber.echo.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Related_News")
public class RelatedNewsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private NewsEntity opp_news;
    @ManyToOne
    private NewsEntity news;
    @Lob
    private String reason;

    @Builder
    public RelatedNewsEntity(NewsEntity opp_news, NewsEntity news, String reason) {
        this.opp_news = opp_news;
        this.news = news;
        this.reason = reason;
    }
}
