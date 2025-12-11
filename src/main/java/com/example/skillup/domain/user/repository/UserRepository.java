package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
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

    @Query("""
    SELECT u
    FROM Users u
    WHERE
        (:deleted = true
        OR :deleted = false AND u.deletedAt IS NULL)
        AND (:keyWard IS NULL OR u.email LIKE %:keyWard% OR u.name LIKE %:keyWard%)
                          
""")
    List<Users> findUsersByKeyWardAndDeleted(@Param("keyWard") String keyWard, @Param("deleted") Boolean deleted);
}
