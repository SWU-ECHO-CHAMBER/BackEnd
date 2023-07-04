package com.echochamber.echo.domain.news.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TopicResponseDto {
    private List<String> result;

    public TopicResponseDto(String jsonString) {
        // JSON 문자열을 처리하여 필드 초기화
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TopicResponseDto responseDto = objectMapper.readValue(jsonString, TopicResponseDto.class);
            this.result = responseDto.getResult();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        int count = 0;

        for (String value : result) {
            str.append(value);
            if (count + 1 != result.size()) {
                str.append(",");
            }
            count++;
        }

        return str.toString();
    }
}
