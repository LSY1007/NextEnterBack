package org.zerock.nextenter.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JWTUtil {

    private static final String key = "1234567890123456789012345678901234567890";

    // JWT 토큰 생성
    public static String generateToken(Map<String, Object> valueMap, int minutes) {
        SecretKey secretKey = null;
        try {
            secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(valueMap)
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(minutes).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    // JWT 토큰 검증
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> claim = null;
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));

            claim = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (MalformedJwtException e) {
            throw new RuntimeException("MalFormed");
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Expired");
        } catch (Exception e) {
            throw new RuntimeException("Error");
        }
        return claim;
    }
}