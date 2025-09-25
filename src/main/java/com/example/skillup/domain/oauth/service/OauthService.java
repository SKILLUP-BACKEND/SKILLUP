package com.example.skillup.domain.oauth.service;


import com.example.skillup.domain.oauth.component.AccessTokenExtractor;
import com.example.skillup.domain.oauth.component.OauthClientFactory;
import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.mappers.UserMapper;
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

    private final OauthClientFactory oauthClientFactory;
    private final UserRepository userRepository;
    private final AccessTokenExtractor accessTokenExtractor;

    public String request(SocialLoginType socialLoginType) {
        return oauthClientFactory.getClient(socialLoginType).getOauthRedirectURL();
    }


    @Transactional
    public Long requestAccessTokenAndSaveUser(SocialLoginType socialLoginType, String code) {

        SocialOauth client = oauthClientFactory.getClient(socialLoginType);

        String accessToken = accessTokenExtractor.extractAccessTokenFromJson(client.requestAccessToken(code));

        if (accessToken == null) {
            throw new OauthException(OauthErrorCode.FAIL_GET_ACCESS_TOKEN);
        }

        String userInfo = client.getUserInfo(accessToken);

        OauthInfo oauthInfo= client.parse(userInfo,accessToken);

        Optional<Users> existingUser = userRepository.findBySocialId(oauthInfo.socialId());


        if(existingUser.isEmpty())
        {
            Users users = UserMapper.of(oauthInfo.email(), oauthInfo.name()
                    , oauthInfo.socialId(), oauthInfo.socialLoginType(), oauthInfo.gender(), oauthInfo.age());
            return userRepository.save(users).getId();
        }
        return existingUser.get().getId();
    }

}
