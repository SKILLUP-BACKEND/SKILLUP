package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.entity.WithdrawReasonCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawReasonCategoryRepository extends JpaRepository<WithdrawReasonCategory, Long>
{
}
