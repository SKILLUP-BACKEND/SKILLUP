package com.example.skillup.domain.oauth.service;


import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.component.AccessTokenExtractor;
import com.example.skillup.domain.oauth.component.OauthClientFactory;
import com.example.skillup.domain.oauth.dto.OauthRequest;
import com.example.skillup.domain.oauth.dto.OauthResponse;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.example.skillup.domain.oauth.mapper.OauthMapper;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserLoginStatus;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.exception.UserErrorCode;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.oauth.component.SocialOauth;
import com.example.skillup.global.auth.service.AuthService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OauthService {

    private final OauthClientFactory oauthClientFactory;
    private final UserRepository userRepository;
    private final AccessTokenExtractor accessTokenExtractor;
    private final TargetRoleRepository targetRoleRepository;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final OauthMapper oauthMapper;

    public String request(SocialLoginType socialLoginType) {
        return oauthClientFactory.getClient(socialLoginType).getOauthRedirectURL();
    }


    @Transactional
    public OauthResponse.OAuthLoginResponse requestAccessTokenAndSaveUser(SocialLoginType socialLoginType, String code) {

        SocialOauth client = oauthClientFactory.getClient(socialLoginType);

        String accessToken = accessTokenExtractor.extractAccessTokenFromJson(client.requestAccessToken(code));

        if (accessToken == null) {
            throw new OauthException(OauthErrorCode.FAIL_GET_ACCESS_TOKEN);
        }

        String userInfo = client.getUserInfo(accessToken);

        OauthRequest oauthInfoRequest= client.parse(userInfo,accessToken);

        Optional<Users> existingUser = userRepository.findBySocialLoginTypeAndSocialIdWithDeleted
                (socialLoginType.name(), oauthInfoRequest.socialId());
        ;

        Users user;
        UserLoginStatus status;

        if (existingUser.isPresent()) {
            user = existingUser.get();


            if(user.getStatus().equals(UserStatus.WITHDRAWN))
            {
                status=UserLoginStatus.WITHDRAW_PENDING_USER;

                return oauthMapper.toWithdrawPendingUserResponse
                        (user.getSocialLoginType().name(),user.getSocialId(),status.name());
            }

            user.updateLastLoginAt(LocalDateTime.now());
            status=UserLoginStatus.EXISTING_USER;
        }
        else {
            Users equalUsers=userRepository.findByEmail(oauthInfoRequest.email()).orElse(null);
            if(equalUsers!=null)
            {
                status = UserLoginStatus.OTHER_OAUTH_USER;
                return oauthMapper.toOtherOauthUserResponse
                        (equalUsers.getSocialLoginType().name(),equalUsers.getEmail(),
                                equalUsers.getSocialId(),status.name());

            }
            user = userMapper.fromOauthInfo(
                    oauthInfoRequest,
                    targetRoleRepository.findByName("기획자").orElseThrow()
            );

            status = UserLoginStatus.NEW_USER;
        }

        userRepository.save(user);
        TokenResponse tokenResponse =authService.login(user.getEmail(),"users");


        return oauthMapper.toOauthLoginResponse(tokenResponse.accessToken(),status.name());
    }

}
