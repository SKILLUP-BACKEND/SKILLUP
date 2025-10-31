package com.example.skillup.global.search.repository;

import com.example.skillup.global.search.enums.SynonymStatus;
import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.exception.SearchErrorCode;
import com.example.skillup.global.search.exception.SearchException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SynonymGroupRepository extends JpaRepository<SynonymGroup, Long> {
    default SynonymGroup getGroup(Long groupId) {
        return findById(groupId).orElseThrow(() -> new SearchException(SearchErrorCode.GROUP_ENTITY_NOT_FOUND,  "Group_ID 가 " + groupId + "인"));
    }
    List<SynonymGroup> findSynonymGroupByStatusAndLocale(SynonymStatus status, String locale);
}
