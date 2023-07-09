package com.echochamber.echo.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "Bookmark")
public class BookmarkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private UserEntity user;
    @ManyToOne
    private NewsEntity news;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public BookmarkEntity(UserEntity user, NewsEntity news) {
        this.user = user;
        this.news = news;
    }
}
