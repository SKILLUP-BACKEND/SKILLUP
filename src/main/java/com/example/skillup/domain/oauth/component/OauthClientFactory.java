package com.example.skillup.domain.oauth.component;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.exception.OauthErrorCode;
import com.example.skillup.domain.oauth.exception.OauthException;
import com.example.skillup.global.auth.oauth.component.SocialOauth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OauthClientFactory {
    private final List<SocialOauth> clients;

    public SocialOauth getClient(SocialLoginType type) {
        return clients.stream()
                .filter(c -> c.getSocialType() == type)
                .findFirst()
                .orElseThrow(() -> new OauthException(OauthErrorCode.UNSUPPORTED_SOCIAL_TYPE));
    }
}

