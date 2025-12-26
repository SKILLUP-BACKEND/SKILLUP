package com.example.skillup.global.search.dto;

import java.util.List;

public record SynonymsSetRequest(List<SynonymsRule> synonyms_set) {
    public record SynonymsRule(String id, String synonyms) {}
}
