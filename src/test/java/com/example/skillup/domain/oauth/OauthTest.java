package com.example.skillup.domain.oauth;


import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.service.OauthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OauthTest
{
    @Autowired
    private OauthService oauthService;

    @Test
    public void Naver_리다이렉트_주소_생성_확인_테스트()
    {
        System.out.println(oauthService.request(SocialLoginType.naver));
    }
    @Test
    public void Kakao_리다이렉트_주소_생성_확인_테스트()
    {
        System.out.println(oauthService.request(SocialLoginType.kakao));
    }

    @Test
    public void Google_리다이렉트_주소_생성_확인_테스트()
    {
        System.out.println(oauthService.request(SocialLoginType.google));
    }
}
