package com.echochamber.echo.global.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StringValidator {
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\." +
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$";

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidNickname(String nickname) {
        // 공백 검사
        if (nickname.isBlank() || StringUtils.containsWhitespace(nickname))
            return false;

        // 길이
        return nickname.length() <= 20 && nickname.length() >= 2;
    }

    public static boolean isValidPassword(String password) {
        // 공백 검사
        if (password.isBlank() || StringUtils.containsWhitespace(password))
            return false;

        // 길이
        return password.length() <= 18 && password.length() >= 8;
    }
}
