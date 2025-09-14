package com.example.skillup.domain.oauth.service;


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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OauthService {

    //private final GoogleOauth googleOauth;
    //private final KakaoOauth kakaoOauth;
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
            //case GOOGLE -> googleOauth;
            //case KAKAO  -> kakaoOauth;
            case NAVER  -> naverOauth;
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

    // 액세스 토큰을 사용하여 사용자 정보를 가져오고, 사용자 정보가 있으면 저장하는 메서드
    public Users requestAccessTokenAndSaveUser(SocialLoginType socialLoginType, String code) {

        String accessTokenJson = requestAccessToken(socialLoginType, code);
        String accessToken = extractAccessTokenFromJson(accessTokenJson);

        if (accessToken == null) {
            throw new OauthException(OauthErrorCode.FAIL_GET_ACCESS_TOKEN);
        }

        String userInfo = getUserInfo(socialLoginType, accessToken);

        Users user = parseUserInfo(userInfo, socialLoginType, accessToken);

        Optional<Users> existingUser = userRepository.findBySocialId(user.getSocialId());
        if (existingUser.isPresent()) {
            Users existing = existingUser.get();
            existing.setAccessToken(user.getAccessToken());
            existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return userRepository.save(existing);
        } else {

            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            return userRepository.save(user);
        }
    }

    private String getUserInfo(SocialLoginType socialLoginType, String accessToken) {
        switch (socialLoginType) {
            case GOOGLE:
                //return googleApiCall(accessToken);
            case KAKAO:
                //return kakaoApiCall(accessToken);
            case NAVER:
                return naverApiCall(accessToken);
            default:
                throw new OauthException(OauthErrorCode.UNSUPPORTED_SOCIAL_TYPE);
            }
        }
    private Users parseUserInfo(String userInfo, SocialLoginType socialLoginType, String accessToken) {
        JsonObject jsonObject = JsonParser.parseString(userInfo).getAsJsonObject();

        String socialId = "";
        String name = "";

        if (socialLoginType == SocialLoginType.GOOGLE)
        {
            socialId = jsonObject.get("sub").getAsString();
            name = jsonObject.get("name").getAsString();
        }
        else if (socialLoginType == SocialLoginType.KAKAO)
        {
            socialId = jsonObject.get("id").getAsString();
            name = jsonObject.getAsJsonObject("properties").get("nickname").getAsString();
        }
        else if (socialLoginType == SocialLoginType.NAVER)
        {
            JsonObject response = jsonObject.getAsJsonObject("response");
            socialId = response.get("id").getAsString();
            name = response.get("name").getAsString();
        }

        Users user = new Users();
        user.setSocialId(socialId);
        user.setName(name);
        user.setProvider(socialLoginType.name());
        user.setAccessToken(accessToken);
        return user;
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
            throw new OauthException(OauthErrorCode.NAVER_SERVER_ERROR);
        }
    }
}
