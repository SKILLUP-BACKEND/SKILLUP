package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.user.entity.RecentSearch;
import com.example.skillup.domain.user.exception.UserErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

    default RecentSearch getRecentSearch(Long recentSearchId) {
        return findById(recentSearchId).orElseThrow(
                () -> new EventException(UserErrorCode.RECENT_SEARCH_ENTITY_NOT_FOUND,
                        "RecentSearchId 가 " + recentSearchId + "인"));
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                DELETE FROM RecentSearch r
                WHERE r.userId = :userId
                  AND r.updatedAt < :since
            """)
    void deleteExpiredByUserId(Long userId, LocalDateTime since);

    Optional<RecentSearch> findByUserIdAndKeyword(Long userId, String keyword);

    @Query("""
                SELECT r
                FROM RecentSearch r
                WHERE r.userId = :userId
                  AND r.updatedAt >= :since
                ORDER BY r.updatedAt DESC
            """)
    List<RecentSearch> findTopByUserIdSinceOrderByUpdatedAtDesc(Long userId, LocalDateTime since, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                DELETE FROM RecentSearch r
                WHERE r.userId = :userId
            """)
    int deleteAllByUserId(Long userId);
}
