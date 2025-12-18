package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.entity.Guest;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuestRepository extends JpaRepository<Guest, String> {

    @Modifying
    @Query("DELETE FROM Guest g WHERE g.expiredAt < :now")
    void deleteExpiredGuests(@Param("now") LocalDateTime now);
}
