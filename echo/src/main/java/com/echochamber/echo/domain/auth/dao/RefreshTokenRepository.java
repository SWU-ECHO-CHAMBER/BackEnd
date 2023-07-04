package com.echochamber.echo.domain.auth.dao;

import com.echochamber.echo.domain.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
}
