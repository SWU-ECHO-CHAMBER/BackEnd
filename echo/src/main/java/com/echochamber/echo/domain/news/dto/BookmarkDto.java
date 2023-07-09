package com.echochamber.echo.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class BookmarkDto {
    private boolean isMarked;
}
