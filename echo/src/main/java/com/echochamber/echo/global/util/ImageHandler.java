package com.echochamber.echo.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class ImageHandler {
    private final String DIR_PATH;

    @Autowired
    public ImageHandler(@Value("${PROFILE_DATABASE_URL}") String path) {
        this.DIR_PATH = path;
    }

    // 프로필 이미지 저장
    public String saveProfileImage(String email, MultipartFile image) throws IOException {
        String file_url = "/" + email + ".jpeg";
        String file_path = DIR_PATH + file_url;
        image.transferTo(new File(file_path));

        return file_url;
    }

    // 프로필 이미지 삭제
    public void deleteProfileImage(String file_url) throws RuntimeException {
        File file = new File(DIR_PATH + file_url);
        boolean result = file.delete();

        // 결과
        if (result) {
            log.info("File deleted: " + file_url);
        } else {
            log.error("File deletion failed: " + file_url);
            throw new RuntimeException("File deletion failed.");
        }
    }
}
