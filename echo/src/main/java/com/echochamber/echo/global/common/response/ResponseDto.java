package com.echochamber.echo.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@RequiredArgsConstructor
public class ResponseDto {
    private final Integer code;
    private final String message;

    public static ResponseDto of(Integer code) {
        return new ResponseDto(code, HttpStatus.valueOf(code).getReasonPhrase());
    }

    public static ResponseDto of(Integer code, String message) {
        return new ResponseDto(code, message);
    }
}
