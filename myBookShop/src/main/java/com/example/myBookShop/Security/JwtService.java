package com.example.myBookShop.Security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    private final String SECRET_KEY;
    private  Long expirationToken;
    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") Long expirationToken
    ) {
        this.SECRET_KEY = secretKey;
        this.expirationToken = expirationToken;
    }
    //Tạo token
    public String generateToken(UserDetails userDetails , Map<String,Object> payload){

        return Jwts.builder().subject(userDetails.getUsername()).
                signWith(getSignKey()).
                issuedAt(new Date()).claims(payload).expiration(new Date(System.currentTimeMillis()+ expirationToken)).
                compact();
    }
    //Tạo khoá
    public SecretKey getSignKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    //Extract token

    //Đọc token
    public Claims readToken(String token){
         return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
    }
    public  <T> T  extractToken(String token, Function<Claims,T> function){
        Claims claims=readToken(token);
        return function.apply(claims);
    }
    //Lấy username
    public String extractUsername(String token ){
        return extractToken(token,Claims::getSubject);
    }
    //Lấy expiration

    public Date extractExpiration(String token){
        return extractToken(token,Claims::getExpiration);
    }
    //Kiểm tra token còn giá trị ko
    public boolean valueOfToken(String token,UserDetails userDetails){
        return (extractUsername(token).equals(userDetails.getUsername()) && extractExpiration(token).after(new Date()));
    }
}
