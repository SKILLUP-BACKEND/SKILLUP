package com.example.skillup.domain.oauth.service;


import com.example.skillup.domain.oauth.Entity.OauthInfo;
import com.example.skillup.domain.oauth.component.AccessTokenExtractor;
import com.example.skillup.domain.oauth.component.OauthClientFactory;
import com.example.skillup.domain.oauth.dto.OauthInfoRequest;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.example.skillup.domain.oauth.mapper.OauthInfoMapper;
import com.example.skillup.domain.oauth.repository.OauthInfoRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.oauth.component.SocialOauth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OauthService {

    private final OauthClientFactory oauthClientFactory;
    private final UserRepository userRepository;
    private final AccessTokenExtractor accessTokenExtractor;
    private final OauthInfoRepository oauthInfoRepository;

    public String request(SocialLoginType socialLoginType) {
        return oauthClientFactory.getClient(socialLoginType).getOauthRedirectURL();
    }


    @Transactional
    public String requestAccessTokenAndSaveUser(SocialLoginType socialLoginType, String code) {

        SocialOauth client = oauthClientFactory.getClient(socialLoginType);

        String accessToken = accessTokenExtractor.extractAccessTokenFromJson(client.requestAccessToken(code));

        if (accessToken == null) {
            throw new OauthException(OauthErrorCode.FAIL_GET_ACCESS_TOKEN);
        }

        String userInfo = client.getUserInfo(accessToken);

        OauthInfoRequest oauthInfoRequest= client.parse(userInfo,accessToken);

        Optional<Users> existingUser = userRepository.findBySocialId(oauthInfoRequest.socialId());


        if(existingUser.isEmpty())
        {
            OauthInfo oauthInfo = OauthInfoMapper.of(oauthInfoRequest.email(), oauthInfoRequest.name()
                    , oauthInfoRequest.socialId(), oauthInfoRequest.socialLoginType(), oauthInfoRequest.gender(), oauthInfoRequest.age());

            return oauthInfoRepository.save(oauthInfo).getEmail();
        }
        return existingUser.get().getEmail();
    }

}
