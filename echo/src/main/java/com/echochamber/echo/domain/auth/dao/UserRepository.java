package com.echochamber.echo.domain.auth.dao;

import com.echochamber.echo.domain.model.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE UserEntity u SET u.nickname = :nickname WHERE u.id = :id")
    void updateNickname(@Param("nickname") String nickname, @Param("id") Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE UserEntity u SET u.encoded_password = :password WHERE u.id = :id")
    void updatePassword(@Param("password") String password, @Param("id") Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE UserEntity u SET u.profileImagePath = :imagePath WHERE u.id = :id")
    void updateProfileImagePath(@Param("imagePath") String imagePath, @Param("id") Long id);
}
