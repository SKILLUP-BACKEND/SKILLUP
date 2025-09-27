package com.example.skillup.global.auth.oauth.component;

import com.example.skillup.domain.oauth.component.HttpClientHelper;
import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NaverOauth implements SocialOauth {
    @Value("${sns.naver.url}")
    private String NAVER_SNS_BASE_URL;

    @Value("${sns.naver.client.id}")
    private String NAVER_SNS_CLIENT_ID;

    @Value("${sns.naver.callback.url}")
    private String NAVER_SNS_CALLBACK_URL;

    @Value("${sns.naver.client.secret}")
    private String NAVER_SNS_CLIENT_SECRET;

    @Value("${sns.naver.token.url}")
    private String NAVER_SNS_TOKEN_BASE_URL;

    private final HttpClientHelper httpClientHelper;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", NAVER_SNS_CLIENT_ID);
        params.put("redirect_uri", NAVER_SNS_CALLBACK_URL);
        params.put("state", "random_state_value");

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return NAVER_SNS_BASE_URL + "?" + parameterString;
    }

    @Override
    public String requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", NAVER_SNS_CLIENT_ID);
        params.add("client_secret", NAVER_SNS_CLIENT_SECRET);
        params.add("redirect_uri", NAVER_SNS_CALLBACK_URL);
        params.add("grant_type", "authorization_code");
        params.add("state", "random_state_value");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(NAVER_SNS_TOKEN_BASE_URL, requestEntity, String.class);

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    return responseEntity.getBody();
                 }
                else {
                    throw new OauthException(OauthErrorCode.OAUTH_TOKEN_ERROR,"네이버 서버에서 토큰 발급 실패");
                }
            }
            catch (HttpServerErrorException e) {
                throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR,"네이버 서버 오류");
            }
    }

    @Override
    public String getUserInfo(String accessToken) {
        return httpClientHelper.get(
                "https://openapi.naver.com/v1/nid/me",
                Map.of("Authorization", "Bearer " + accessToken)
        );
    }

    @Override
    public OauthInfo parse(String userInfo, String accessToken) {
        JsonObject response = JsonParser.parseString(userInfo)
                .getAsJsonObject()
                .getAsJsonObject("response");

        String socialId = response.get("id").getAsString();
        String name = response.get("name").getAsString();

        String email = response.has("email") && !response.get("email").isJsonNull()
                ? response.get("email").getAsString()
                : null;

        String gender = response.has("gender") && !response.get("gender").isJsonNull()
                ? response.get("gender").getAsString()
                : null;

        String age = response.has("age") && !response.get("age").isJsonNull()
                ? response.get("age").getAsString()
                : null;

        return OauthInfo.of(email, name, socialId, getSocialType(), gender, age);
    }

}