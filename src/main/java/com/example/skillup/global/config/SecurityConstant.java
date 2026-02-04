package com.example.skillup.global.config;

import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConstant {
    public static final String[] PUBLIC_AUTH_URLS = {

            // ========== Events (public) ==========
            "/events/*",                       // GET /events/{eventId}
            "/events/home/featured",           // GET
            "/events/home/closing-soon",       // GET
            "/events/home/category",           // GET
            "/events/home/banners",            // GET
            "/events/search/home",             // POST
            "/events/home/apply/banner/*",     // POST

            // Category page
            "/events/category-page/search",     // POST
            "/events/category-page/recommended",// GET

            // Home
            "/events/home/recent",             // GET
            "/events/*/apply"  ,                // PATCH /events/{eventId}/apply

            // ========== Articles (public) ==========
            "/articles/search",                 // GET
            "/articles",                        // GET
            "/articles/read/*",                  // POST /articles/read/{articleId}

            // ========== User (public) ==========
            "/user/test-login",                  // GET (테스트용)
            "/user/my-page/profile/interest",    // GET
            "/user/my-page/with-draw/category",   // GET
            "/user/my-page/qna",   // GET

            // ========== OAuth (public) ==========
            "/oauth/**",
            // ========== Admin ==========
            "/admin/login",
            // ========== Test ==========
            "/api/test/**"
    };

    // Swagger UI 관련 공개 경로
    public static final String[] SWAGGER_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // Admin 관련 API 경로
    public static final String[] ADMIN_URLS = {
    };

    // 모든 공개 URL들
    public static final String[] PUBLIC_URLS =
            Stream.of(PUBLIC_AUTH_URLS, SWAGGER_URLS)
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new);
}
