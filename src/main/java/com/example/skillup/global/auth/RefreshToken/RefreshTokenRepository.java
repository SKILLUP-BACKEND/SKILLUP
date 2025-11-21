package com.example.skillup.global.auth.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>
{
    @Modifying
    @Query("UPDATE RefreshToken r SET r.refreshToken = :refreshToken WHERE r.email = :email")
    int updateTokenByUserId(@Param("email") String email, @Param("refreshToken") String refreshToken);

    Optional<RefreshToken> findByEmail(String email);

}
