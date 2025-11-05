package com.example.skillup.global.search.repository;

import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.entity.SynonymTerm;
import com.example.skillup.global.search.enums.SynonymStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SynonymTermRepository extends JpaRepository<SynonymTerm, Long> {
    @Query("""
        select t.group.id as gid, t.term as term
        from SynonymTerm t
        where t.status = 'ACTIVE' and t.group.status = 'ACTIVE' and t.group.locale = :locale
        """)
    List<Object[]> findAllActiveFlat(@Param("locale") String locale);

    default Map<Long, List<String>> findAllActiveGrouped(String locale) {
        Map<Long, List<String>> map = new HashMap<>();
        for (Object[] row : findAllActiveFlat(locale)) {
            Long gid = (Long) row[0];
            String term = (String) row[1];
            map.computeIfAbsent(gid, k -> new ArrayList<>()).add(term);
        }
        return map;
    }

    List<SynonymTerm> findAllByGroupAndStatus(SynonymGroup group, SynonymStatus status);

    List<SynonymTerm> findAllByGroup(SynonymGroup group);
}
