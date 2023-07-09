package com.echochamber.echo.domain.auth.domain;

import com.echochamber.echo.domain.model.UserEntity;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
@Slf4j
public class GoogleAuth {
    private final String CLIENT_ID;
    private final String provider = "google";

    @Autowired
    public GoogleAuth(@Value("${GOOGLE_OAUTH_CLIENT_ID}") String CLIENT_ID) {
        this.CLIENT_ID = CLIENT_ID;
    }

    public UserEntity authenticate(String token) throws GeneralSecurityException, IOException {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        if (token != null) {

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Get profile information from payload
                String email = payload.getEmail();
                String nickname = (String) payload.get("given_name");
                String profileImagePath = (String) payload.get("picture");

                return new UserEntity(email, nickname, provider, profileImagePath);
            } else {
                log.error("Invalid ID token.");
            }
        }

        return null;
    }
}
