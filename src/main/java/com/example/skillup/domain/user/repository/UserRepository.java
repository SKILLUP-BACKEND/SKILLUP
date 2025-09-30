package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long>
{
    Optional<Users> findByEmail(String email);
    Optional<Users> findBySocialId(String socialId);

}
