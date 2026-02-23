package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.entity.Users;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findBySocialLoginTypeAndSocialId(
            SocialLoginType socialLoginType,
            String socialId
    );

    @Query(
            value = """
                    SELECT *
                    FROM users u
                    WHERE
                        (:deleted = true
                         OR (:deleted = false AND u.deleted_at IS NULL))
                      AND (:keyword IS NULL
                           OR u.email LIKE CONCAT('%', :keyword, '%')
                           OR u.name  LIKE CONCAT('%', :keyword, '%'))
                    ORDER BY u.created_at DESC
                    """,
            nativeQuery = true
    )
    List<Users> findUsersByKeyWardAndDeleted(@Param("keyword") String keyword, @Param("deleted") Boolean deleted,
                                             Pageable pageable);

    @Query(value = """
            SELECT
                COALESCE(SUM(ea.action_type = 'VIEW') ,0) AS viewCnt,
                COALESCE(SUM(ea.action_type = 'APPLY') ,0) AS applyCnt,
                (
                    SELECT COUNT(*)
                    FROM event_bookmark eb
                    WHERE eb.user_id = :userId
                      AND eb.is_bookmarked = true
                      AND eb.deleted_at IS NULL
                ) AS saveCnt
            FROM event_action ea
            WHERE ea.actor_id = :actorId
            AND ea.deleted_at IS NULL
            """, nativeQuery = true)
    EventActionCountProjection getUserActionCounts(@Param("actorId") String actorId , @Param("userId") Long userId);

    @Query(
            value = """
                    SELECT r.name
                    from Users u
                    join u.role r
                    where u = :user
                    """
    )
    String findByRoleNameByUsers(@Param("user") Users user);

    public interface EventActionCountProjection {
        int getViewCnt();

        int getApplyCnt();

        int getSaveCnt();
    }

    @Query(value = """
            SELECT COUNT(*)
            FROM Users u
            WHERE u.deletedAt IS NULL
            """)
    int getTotalCount();


    @Query(
            value = "SELECT * FROM users WHERE id = :userId",
            nativeQuery = true
    )
    Optional<Users> findByIdNative(@Param("userId") Long userId);


    @Modifying
    @Query("""
                DELETE FROM Users u
                WHERE u.role = :guestRole
                  AND u.deletedAt IS NOT NULL
                  AND u.deletedAt <= :threshold
            """)
    void deleteExpiredUsers(@Param("threshold") LocalDateTime threshold);
}
