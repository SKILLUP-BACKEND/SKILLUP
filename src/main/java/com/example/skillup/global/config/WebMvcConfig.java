package com.example.skillup.global.config;

import com.example.skillup.global.interceptor.GuestIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final GuestIdInterceptor guestIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(guestIdInterceptor)
                .addPathPatterns(
                        "/events/home/recent",
                        "/events/*/apply",
                        "/events/*"
                );
    }
}
