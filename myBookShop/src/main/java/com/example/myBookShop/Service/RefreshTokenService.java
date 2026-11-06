package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Respone.TokenResponse;
import com.example.myBookShop.Entity.RefreshToken;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Exception.TokenException;
import com.example.myBookShop.Repositories.RfTokenRepository;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final long expirationToken;
    private final RfTokenRepository rfTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RfTokenRepository rfTokenRepository, UserRepository userRepository, JwtService jwtService,
                               @Value("${jwt.expiration-refreshToken}") long expirationToken) {
        this.rfTokenRepository = rfTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.expirationToken = expirationToken;
    }

    /**
     * Tạo refresh token mới. Token gốc chỉ trả cho client một lần,
     * DB chỉ lưu bản băm nên lộ DB cũng không dùng được token.
     */
    @Transactional
    public String create(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return issueFor(user);
    }

    /**
     * Xoay vòng token: mỗi refresh token chỉ dùng được một lần.
     * Token cũ bị xoá, client nhận cặp access token và refresh token mới.
     */
    @Transactional
    public TokenResponse rotate(String rawToken) {
        RefreshToken current = rfTokenRepository.findByToken(hash(rawToken))
                .orElseThrow(() -> new TokenException("Refresh token is invalid"));
        if (current.isRevoked()) throw new TokenException("Refresh token is revoked");
        if (current.getExpiryAt().isBefore(Instant.now())) throw new TokenException("Refresh token is expired");

        User user = current.getUsersToken();
        if (!user.isEnabled()) throw new TokenException("Account is disabled");

        String newRefreshToken = issueFor(user);
        return new TokenResponse(jwtService.generateToken(user), newRefreshToken);
    }

    /** Đăng xuất: xoá mọi refresh token của user. Token không tồn tại thì coi như đã đăng xuất. */
    @Transactional
    public void revoke(String rawToken) {
        rfTokenRepository.findByToken(hash(rawToken))
                .ifPresent(token -> rfTokenRepository.deleteAllByUsersToken_Id(token.getUsersToken().getId()));
    }

    private String issueFor(User user) {
        rfTokenRepository.deleteAllByUsersToken_Id(user.getId());

        String rawToken = UUID.randomUUID().toString();
        Instant now = Instant.now();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(hash(rawToken));
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiryAt(now.plusMillis(expirationToken));
        refreshToken.setRevoked(false);
        refreshToken.setUsersToken(user);
        rfTokenRepository.save(refreshToken);
        return rawToken;
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
