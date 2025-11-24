package com.example.skillup.global.auth.jwt;

import com.example.skillup.global.auth.RefreshToken.RefreshToken;
import com.example.skillup.global.auth.RefreshToken.RefreshTokenRepository;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.exception.GlobalException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthService authService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);


        if (token != null) {

            if (!jwtProvider.validateSignature(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        CommonErrorCode.REFRESH_TOKEN_INVALID.getMessage());
                return;
            }
            if (jwtProvider.isExpired(token)) {

                String email = jwtProvider.getClaims(token).getSubject();


                RefreshToken refreshToken=authService.getRefreshTokenByUserId(email);

                if (refreshToken==null||!jwtProvider.validateToken(refreshToken.getRefreshToken())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            CommonErrorCode.REFRESH_TOKEN_EXPIRED.getMessage());
                    return;
                }

                String newAccessToken = jwtProvider.generateToken(email, "users", Duration.ofHours(1));
                response.setHeader("Authorization", "Bearer " + newAccessToken);

                Authentication auth = jwtProvider.getAuthentication(newAccessToken);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                Authentication auth = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
