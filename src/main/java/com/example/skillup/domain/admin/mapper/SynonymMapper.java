package com.example.skillup.domain.admin.mapper;

import com.example.skillup.domain.admin.dto.SynonymRequest.CreateSynonymRequest;
import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.entity.SynonymTerm;
import com.example.skillup.global.search.enums.SynonymStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SynonymMapper {
    public SynonymGroup toGroupEntity(CreateSynonymRequest request) {
        return SynonymGroup.builder()
                .locale(request.locale())
                .comment(request.comment())
                .status(SynonymStatus.ACTIVE)
                .build();
    }

    public List<SynonymTerm> toTermEntity(SynonymGroup synonymGroup, List<String> terms) {
        terms = terms.stream().map(String::trim).toList();
        List<SynonymTerm> synonymTermList = new ArrayList<>();
        for (String term : terms) {
            synonymTermList.add(SynonymTerm.builder()
                    .group(synonymGroup)
                    .term(term)
                    .status(SynonymStatus.ACTIVE)
                    .build());
        }
        return synonymTermList;
    }
}
