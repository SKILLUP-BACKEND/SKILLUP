package com.example.skillup.domain.oauth.repository;

import com.example.skillup.domain.oauth.Entity.OauthInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthInfoRepository extends JpaRepository<OauthInfo, Long> {
}
