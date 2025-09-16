package com.example.skillup.domain.oauth.service;


import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.oauth.component.GoogleOauth;
import com.example.skillup.global.auth.oauth.component.KakaoOauth;
import com.example.skillup.global.auth.oauth.component.NaverOauth;
import com.example.skillup.global.auth.oauth.component.SocialOauth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OauthService {

    private final GoogleOauth googleOauth;
    private final KakaoOauth kakaoOauth;
    private final NaverOauth naverOauth;

    private final UserRepository userRepository;

    public String request(SocialLoginType socialLoginType) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        return socialOauth.getOauthRedirectURL();
    }

    public String requestAccessToken(SocialLoginType socialLoginType, String code) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        return socialOauth.requestAccessToken(code);
    }

    private SocialOauth findSocialOauthByType(SocialLoginType socialLoginType) {
        return switch (socialLoginType) {
            case google -> googleOauth;
            case kakao  -> kakaoOauth;
            case naver  -> naverOauth;
            default -> throw new OauthException(OauthErrorCode.UNSUPPORTED_SOCIAL_TYPE);
        };
    }

    private String extractAccessTokenFromJson(String accessTokenJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(accessTokenJson);

            return jsonNode.get("access_token") != null ? jsonNode.get("access_token").asText() : null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public Long requestAccessTokenAndSaveUser(SocialLoginType socialLoginType, String code) {

        String accessTokenJson = requestAccessToken(socialLoginType, code);
        String accessToken = extractAccessTokenFromJson(accessTokenJson);

        if (accessToken == null) {
            throw new OauthException(OauthErrorCode.FAIL_GET_ACCESS_TOKEN);
        }

        String userInfo = getUserInfo(socialLoginType, accessToken);

        OauthInfo oauthInfo = parseOauthInfo(userInfo, socialLoginType, accessToken);

        Optional<Users> existingUser = userRepository.findBySocialId(oauthInfo.socialId());


        if(existingUser.isEmpty())
        {
            Users users = Users.of(oauthInfo.email(),oauthInfo.name()
                    ,oauthInfo.socialId(),oauthInfo.socialLoginType(),oauthInfo.gender(),oauthInfo.age());
            return userRepository.save(users).getId();
        }
        return existingUser.get().getId();
    }

    private String getUserInfo(SocialLoginType socialLoginType, String accessToken) {
        switch (socialLoginType) {
            case google:
                return googleApiCall(accessToken);
            case kakao:
                return kakaoApiCall(accessToken);
            case naver:
                return naverApiCall(accessToken);
            default:
                throw new OauthException(OauthErrorCode.UNSUPPORTED_SOCIAL_TYPE);
            }
        }
    private OauthInfo parseOauthInfo(String userInfo, SocialLoginType socialLoginType, String accessToken) {
        JsonObject jsonObject = JsonParser.parseString(userInfo).getAsJsonObject();

        String socialId = "";
        String name = "";
        String email = "";
        String gender = null;
        String age=null;

        if (socialLoginType == SocialLoginType.google) {
            socialId = jsonObject.get("sub").getAsString();
            name = jsonObject.get("name").getAsString();
            email = jsonObject.get("email").getAsString();

            if (jsonObject.has("gender") && !jsonObject.get("gender").isJsonNull()) {
                gender = jsonObject.get("gender").getAsString();
            }
            if (jsonObject.has("birthdate") && !jsonObject.get("birthdate").isJsonNull()) {
                age = jsonObject.get("birthdate").getAsString();
            }
        }
        else if (socialLoginType == SocialLoginType.kakao) {
            socialId = jsonObject.get("id").getAsString();

            JsonObject kakaoAccount = jsonObject.getAsJsonObject("kakao_account");
            name = jsonObject.getAsJsonObject("properties").get("nickname").getAsString();

            if (kakaoAccount.has("email") && !kakaoAccount.get("email").isJsonNull()) {
                email = kakaoAccount.get("email").getAsString();
            }
            if (kakaoAccount.has("gender") && !kakaoAccount.get("gender").isJsonNull()) {
                gender = kakaoAccount.get("gender").getAsString();
            }
            if (kakaoAccount.has("age_range") && !kakaoAccount.get("age_range").isJsonNull()) {
                age = kakaoAccount.get("age_range").getAsString();
            }
        }
        else if (socialLoginType == SocialLoginType.naver) {
            JsonObject response = jsonObject.getAsJsonObject("response");
            socialId = response.get("id").getAsString();
            name = response.get("name").getAsString();

            if (response.has("email") && !response.get("email").isJsonNull()) {
                email = response.get("email").getAsString();
            }
            if (response.has("gender") && !response.get("gender").isJsonNull()) {
                gender = response.get("gender").getAsString();
            }
            if (response.has("age") && !response.get("age").isJsonNull()) {
                age = response.get("age").getAsString();
            }
        }

        return OauthInfo.of(email,name, Long.valueOf(socialId),socialLoginType,gender,age);
    }

    private String naverApiCall(String accessToken) {
        try {
            String url = "https://openapi.naver.com/v1/nid/me";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                throw new OauthException(OauthErrorCode.FAIL_GET_USER_INFO);
            }
        } catch (IOException e) {
            throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR);
        }
    }

    private String kakaoApiCall(String accessToken) {
        try {
            String url = "https://kapi.kakao.com/v2/user/me";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                throw new OauthException(OauthErrorCode.FAIL_GET_USER_INFO);
            }
        } catch (IOException e) {
            throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR);
        }
    }

    public String googleApiCall(String accessToken) {
        try {
            String encodedAccessToken = URLEncoder.encode(accessToken, "UTF-8");

            String url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + encodedAccessToken;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            int responseCode = con.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer errorResponse = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    errorResponse.append(inputLine);
                }
                in.close();
                throw new OauthException(OauthErrorCode.FAIL_GET_USER_INFO
                        ,"Google API에서 사용자 정보를 가져오는 데 실패했습니다. 응답 코드: " + responseCode + ", 에러 메시지: " + errorResponse);
            }
        } catch (IOException e) {
            throw new OauthException(OauthErrorCode.OAUTH_SERVER_ERROR);
        }
    }
}
