package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.HashTag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HashTagRepository extends JpaRepository<HashTag,Long> {
    Optional<HashTag> findByName(String name);

    @Query(value = """
    SELECT ht.*
    FROM hash_tag ht
    JOIN (
        SELECT activity_tag_weights.hash_tags_id,
               SUM(activity_tag_weights.weight) AS score
        FROM (

            SELECT eht2.hash_tags_id AS hash_tags_id,
                   0.3E0 AS weight
            FROM event_action ea
            JOIN event_hash_tags eht2 ON ea.event_id = eht2.event_id
            WHERE ea.actor_id = :actorId
              AND ea.created_at >= :since
              AND ea.action_type = 'VIEW'

            UNION ALL

            SELECT eht3.hash_tags_id AS hash_tags_id,
                   0.6E0 AS weight
            FROM event_bookmark eb
            JOIN event_hash_tags eht3 ON eb.event_id = eht3.event_id
            WHERE eb.user_id = :userId
              AND eb.deleted_at IS NULL
              AND eb.is_bookmarked = true
              AND eb.updated_at >= :since

            UNION ALL

            SELECT eht4.hash_tags_id AS hash_tags_id,
                   0.1E0 AS weight
            FROM event_action ea2
            JOIN event_hash_tags eht4 ON ea2.event_id = eht4.event_id
            WHERE ea2.actor_id = :actorId
              AND ea2.created_at >= :since
              AND ea2.action_type = 'APPLY'
        ) AS activity_tag_weights
        GROUP BY activity_tag_weights.hash_tags_id
        ORDER BY score DESC
        LIMIT 6
    ) top_tags ON ht.id = top_tags.hash_tags_id
    ORDER BY top_tags.score DESC
""", nativeQuery = true)
    List<HashTag> findUserTopHashTagEntitiesTop6(
            @Param("actorId") String actorId,
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

}
