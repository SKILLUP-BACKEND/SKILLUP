package com.example.skillup.global.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GuestIdInterceptor implements HandlerInterceptor {

    public static final String GUEST_COOKIE_NAME = "guest_id";
    public static final String GUEST_ATTRIBUTE_NAME = "guest_id_attr";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String method = request.getMethod();
        if (!HttpMethod.GET.matches(method) && !HttpMethod.PATCH.matches(method)) {
            return true;
        }
        
        String guestId = null;
        if (request.getCookies() != null) {
            guestId = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(GUEST_COOKIE_NAME))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (guestId == null) {
            guestId = UUID.randomUUID().toString();
        }

        ResponseCookie cookie = ResponseCookie.from(GUEST_COOKIE_NAME, guestId)
                .path("/")
                .httpOnly(true)
                .maxAge(60 * 60 * 24 * 30)
                .sameSite("None")
                .secure(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        request.setAttribute(GUEST_ATTRIBUTE_NAME, guestId);

        return true;
    }
}
