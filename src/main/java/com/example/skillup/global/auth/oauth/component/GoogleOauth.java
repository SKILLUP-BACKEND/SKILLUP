package com.example.skillup.global.auth.oauth.component;

import com.example.skillup.domain.oauth.component.HttpClientHelper;
import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauth {
    @Value("${sns.google.url}")
    private String GOOGLE_SNS_BASE_URL;
    @Value("${sns.google.client.id}")
    private String GOOGLE_SNS_CLIENT_ID;
    @Value("${sns.google.callback.url}")
    private String GOOGLE_SNS_CALLBACK_URL;
    @Value("${sns.google.client.secret}")
    private String GOOGLE_SNS_CLIENT_SECRET;
    @Value("${sns.google.token.url}")
    private String GOOGLE_SNS_TOKEN_BASE_URL;

    private final HttpClientHelper httpClientHelper;


    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", "profile");
        params.put("response_type", "code");
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return GOOGLE_SNS_BASE_URL + "?" + parameterString;
    }

    @Override
    public String requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("client_secret", GOOGLE_SNS_CLIENT_SECRET);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);
        params.put("grant_type", "authorization_code");

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(GOOGLE_SNS_TOKEN_BASE_URL, params, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            }
            else {
                throw new OauthException(OauthErrorCode.OAUTH_TOKEN_ERROR,"구글 서버에서 토큰 발급 실패");
            }
        }
        catch (HttpServerErrorException e) {
            throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR,"구글 서버 에러");
        }
    }

    @Override
    public String getUserInfo(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" +
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        return httpClientHelper.get(
                url,
                Map.of("Content-Type", "application/json")
        );
    }

    @Override
    public OauthInfo parse(String userInfo, String accessToken) {
        JsonObject jsonObject = JsonParser.parseString(userInfo).getAsJsonObject();

        String socialId = jsonObject.get("sub").getAsString();
        String name = jsonObject.get("name").getAsString();
        String email = jsonObject.get("email").getAsString();

        String gender = jsonObject.has("gender") && !jsonObject.get("gender").isJsonNull()
                ? jsonObject.get("gender").getAsString()
                : null;

        String age = jsonObject.has("birthdate") && !jsonObject.get("birthdate").isJsonNull()
                ? jsonObject.get("birthdate").getAsString()
                : null;

        return OauthInfo.of(email, name, Long.valueOf(socialId), getSocialType(), gender, age);
    }

}
