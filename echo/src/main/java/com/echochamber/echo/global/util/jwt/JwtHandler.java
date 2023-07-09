package com.echochamber.echo.global.util.jwt;

import com.echochamber.echo.domain.model.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;

@Component
public class JwtHandler {
    private final String secretKey;

    public JwtHandler(@Value("${JWT_SECRET_KEY}") String secretKey) {
        this.secretKey = secretKey;
    }

    // Bearer 해석
    public String decodeBearer(String str) {
        return Arrays.stream(str.split("Bearer")).toList().get(1);
    }

    // 토큰 발행
    public String generateToken(boolean isAccessToken, UserEntity user) {
        // payloads 생성
        Map<String, Object> payloads = new LinkedHashMap<>();
        payloads.put("userId", user.getId());
        payloads.put("email", user.getEmail());

        Date now = new Date();
        Duration duration = isAccessToken ? Duration.ofHours(2) : Duration.ofDays(7);
        Date expiration = new Date(now.getTime() + duration.toMillis());
        String subject = isAccessToken ? "accessToken" : "refreshToken";

        return builder().setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(payloads)
                .setIssuer("admin")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setSubject(subject)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .compact();
    }

    // 토큰 검증
    public Map<String, Object> verifyJWT(String jwt)
            throws InvalidClaimException, ExpiredJwtException {
        return parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(jwt)
                .getBody();
    }
}
