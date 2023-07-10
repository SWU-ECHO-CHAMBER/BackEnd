package com.echochamber.echo.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "User")
@DynamicInsert
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String nickname;
    @Column(columnDefinition = "TEXT")
    private String profileImagePath;
    private String encoded_password;
    private String provider;
    @CreationTimestamp
    private LocalDateTime createdAt;

    public UserEntity(String email, String nickname, String provider, String profileImagePath) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.profileImagePath = profileImagePath;
    }

    public UserEntity(String email, String nickname, String encoded_password, String provider, String profileImagePath) {
        this.email = email;
        this.nickname = nickname;
        this.encoded_password = encoded_password;
        this.provider = null;
        this.profileImagePath = profileImagePath;
    }
}
