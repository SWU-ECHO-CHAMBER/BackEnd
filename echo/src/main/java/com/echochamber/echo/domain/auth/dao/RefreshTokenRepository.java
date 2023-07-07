package com.echochamber.echo.domain.auth.dao;

import com.echochamber.echo.domain.model.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, Long> {
    void deleteByUserId(Long userId);
}
