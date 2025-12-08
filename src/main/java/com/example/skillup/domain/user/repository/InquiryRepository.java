package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.user.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long>
{
    List<Inquiry> findAllByOrderByIdAsc();
}
