package com.example.skillup.domain.oauth.component;

import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenExtractor {

    private final ObjectMapper objectMapper;

    public AccessTokenExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String extractAccessTokenFromJson(String accessTokenJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(accessTokenJson);

            return jsonNode.get("access_token") != null ? jsonNode.get("access_token").asText() : null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}