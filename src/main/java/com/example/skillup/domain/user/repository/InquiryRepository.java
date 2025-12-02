package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long>
{
}
