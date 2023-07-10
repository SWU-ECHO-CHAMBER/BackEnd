package com.echochamber.echo.domain.auth.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LoginResponseDto {
    private Long user_id;
    private String access_token;
    private String refresh_token;

    @Builder
    public LoginResponseDto(Long user_id, Map<String, String> tokens) {
        this.user_id = user_id;
        this.access_token = tokens.get("access_token");
        this.refresh_token = tokens.get("refresh_token");
    }
}
