package com.example.skillup.global.auth.oauth.component;

import com.example.skillup.domain.oauth.component.HttpClientHelper;
import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KakaoOauth implements SocialOauth {

    @Value("${sns.kakao.url}")
    private String KAKAO_SNS_BASE_URL;
    @Value("${sns.kakao.client.id}")
    private String KAKAO_SNS_CLIENT_ID;
    @Value("${sns.kakao.callback.url}")
    private String KAKAO_SNS_CALLBACK_URL;
    @Value("${sns.kakao.client.secret}")
    private String KAKAO_SNS_CLIENT_SECRET;
    @Value("${sns.kakao.token.url}")
    private String KAKAO_SNS_TOKEN_BASE_URL;

    private final HttpClientHelper httpClientHelper;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", KAKAO_SNS_CLIENT_ID);
        params.put("redirect_uri", KAKAO_SNS_CALLBACK_URL);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return KAKAO_SNS_BASE_URL + "?" + parameterString;
    }

    @Override
    public String requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", KAKAO_SNS_CLIENT_ID);
        params.add("client_secret", KAKAO_SNS_CLIENT_SECRET);
        params.add("redirect_uri", KAKAO_SNS_CALLBACK_URL);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(KAKAO_SNS_TOKEN_BASE_URL, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            }
            else {
                throw new OauthException(OauthErrorCode.OAUTH_TOKEN_ERROR,"카카오 서버에서 토큰 발급 실패");
            }
        }
        catch (HttpServerErrorException e) {
            throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR,"카카오 서버 에러");
        }
    }

    @Override
    public String getUserInfo(String accessToken) {
        return httpClientHelper.get(
                "https://kapi.kakao.com/v2/user/me",
                Map.of("Authorization", "Bearer " + accessToken)
        );
    }

    @Override
    public OauthInfo parse(String userInfo, String accessToken) {
        JsonObject jsonObject = JsonParser.parseString(userInfo).getAsJsonObject();

        String socialId = jsonObject.get("id").getAsString();
        JsonObject kakaoAccount = jsonObject.getAsJsonObject("kakao_account");
        String name = jsonObject.getAsJsonObject("properties").get("nickname").getAsString();

        String email = kakaoAccount.has("email") && !kakaoAccount.get("email").isJsonNull()
                ? kakaoAccount.get("email").getAsString()
                : null;

        String gender = kakaoAccount.has("gender") && !kakaoAccount.get("gender").isJsonNull()
                ? kakaoAccount.get("gender").getAsString()
                : null;

        String age = kakaoAccount.has("age_range") && !kakaoAccount.get("age_range").isJsonNull()
                ? kakaoAccount.get("age_range").getAsString()
                : null;

        return OauthInfo.of(email, name, socialId, getSocialType(), gender, age);
    }

}