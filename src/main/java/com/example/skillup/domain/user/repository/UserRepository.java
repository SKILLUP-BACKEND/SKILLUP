package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long>
{
}
