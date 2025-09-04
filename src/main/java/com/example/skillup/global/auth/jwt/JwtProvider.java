package com.example.skillup.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(Long id, String role, Duration expiredAt)
    {
        String subject = id==null ? "admin":  id.toString();
        Date now = new Date();

        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(jwtProperties.getIssuer())
                .claim("roles", role)
                .setIssuedAt(new Date())
                .setExpiration((new Date(now.getTime() + expiredAt.toMillis())))
                .signWith( getSigningKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token)
    {
        try{

             Jwts.parserBuilder()
                    .setSigningKey( getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication (String token)
    {
        Claims claims = getClaims(token);
        String role = claims.get("roles", String.class);

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        return new UsernamePasswordAuthenticationToken( new org.springframework.security.core.
                userdetails.User(claims.getSubject(),"",authorities ),
                token, authorities);
    }

    private Claims getClaims(String token)
    {

        return Jwts.parserBuilder()
                .setSigningKey( getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
