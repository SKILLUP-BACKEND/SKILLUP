package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long>
{

    Optional<Users> findByEmail(String email);
    Optional<Users> findBySocialId(String socialId);

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
    List<Users> findUsersByKeyWardAndDeleted(@Param("keyWard") String keyWard, @Param("deleted") Boolean deleted,Pageable pageable);

    @Query(value = """
SELECT
    COALESCE(SUM(ea.action_type = 'VIEW') AS viewCnt,0),
    COALESCE(SUM(ea.action_type = 'APPLY') AS applyCnt,0),
    COALESCE(SUM(ea.action_type = 'SAVE') AS saveCnt,0)
FROM event_action ea
WHERE ea.actorId = :actorId
""", nativeQuery = true)
    EventActionCountProjection getUserActionCounts(@Param("actorId") String actorId);

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
}
