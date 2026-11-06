package com.example.myBookShop.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey signKey;
    private final long expirationToken;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long expirationToken
    ) {
        // Tạo khoá một lần khi khởi động. Secret ngắn hơn 32 byte sẽ báo lỗi ngay từ đầu.
        this.signKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationToken = expirationToken;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationToken))
                .signWith(signKey)
                .compact();
    }

    /** parseSignedClaims tự kiểm tra chữ ký và thời hạn, token không hợp lệ sẽ ném JwtException. */
    public Claims readToken(String token) {
        return Jwts.parser().verifyWith(signKey).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return readToken(token).getSubject();
    }
}
