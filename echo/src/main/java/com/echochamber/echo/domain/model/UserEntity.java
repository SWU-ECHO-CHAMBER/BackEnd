package com.echochamber.echo.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
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
    private String password;
    @Lob
    private String passwordSalt;
    private String provider;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public UserEntity(String email, String nickname, String provider) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
    }

    @Builder
    public UserEntity(String email, String nickname, String password, String passwordSalt) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.passwordSalt = passwordSalt;
    }
}
