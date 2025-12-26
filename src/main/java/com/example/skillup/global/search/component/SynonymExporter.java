package com.example.skillup.global.search.component;


import com.example.skillup.global.search.dto.SynonymsSetRequest;
import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.enums.SynonymStatus;
import com.example.skillup.global.search.repository.SynonymGroupRepository;
import com.example.skillup.global.search.repository.SynonymTermRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SynonymExporter {

    private final SynonymGroupRepository synonymGroupRepository;
    private final SynonymTermRepository synonymTermRepository;

    @Transactional(readOnly = true)
    public List<SynonymsSetRequest.SynonymsRule> exportActiveRules(String locale) {
        List<SynonymGroup> groups =
                synonymGroupRepository.findSynonymGroupByStatusAndLocale(SynonymStatus.ACTIVE, locale);

        Map<Long, List<String>> termsByGroup =
                synonymTermRepository.findAllActiveGrouped(locale);

        List<SynonymsSetRequest.SynonymsRule> rules = new ArrayList<>();

        for (SynonymGroup g : groups) {
            List<String> terms = termsByGroup.getOrDefault(g.getId(), List.of()).stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();

            String synonymsSolr = String.join(", ", terms);
            String ruleId = "g-" + g.getId();

            rules.add(new SynonymsSetRequest.SynonymsRule(ruleId, synonymsSolr));
        }

        return rules;
    }
}
