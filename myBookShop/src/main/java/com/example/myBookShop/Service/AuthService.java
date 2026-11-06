package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.LoginRequest;
import com.example.myBookShop.DTO.Request.RegisterRequest;
import com.example.myBookShop.DTO.Request.VerifyRegisterRequest;
import com.example.myBookShop.DTO.Respone.TokenResponse;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.InvalidRequestException;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Security.JwtService;
import com.example.myBookShop.untils.GenerateCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private static final Duration REGISTER_CODE_TTL = Duration.ofMinutes(5);
    private static final int MAX_CODE_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final GenerateCode generateCode;

    /**
     * AuthenticationManager mới là nơi so khớp mật khẩu với bản BCrypt trong DB.
     * Sai mật khẩu, sai username hoặc tài khoản bị khoá đều ném AuthenticationException.
     */
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassWord()));
        User user = (User) authentication.getPrincipal();
        String refreshToken = refreshTokenService.create(user.getId());
        return new TokenResponse(jwtService.generateToken(user), refreshToken);
    }

    /**
     * Bước 1 của đăng ký: chưa ghi vào DB, chỉ giữ thông tin tạm trong Redis và tạo mã xác nhận.
     * Server không bao giờ được dừng lại để chờ nhập từ console như Scanner(System.in).
     */
    public void startRegistration(RegisterRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new ConflictDataException("Username is existed!");
        }
        String code = generateCode.genCode();
        PendingRegistration pending = new PendingRegistration(
                request.getUserName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassWord()),
                request.getPhone(),
                code);
        redisTemplate.delete(attemptsKey(request.getUserName()));
        redisTemplate.opsForValue().set(pendingKey(request.getUserName()), pending, REGISTER_CODE_TTL);
        // Chỉ dùng khi phát triển. Khi có dịch vụ email thì thay dòng này bằng gửi email, không log mã.
        log.info("Register code for {}: {}", request.getUserName(), code);
    }

    /** Bước 2 của đăng ký: mã đúng mới tạo user. Giới hạn số lần thử để chống dò mã. */
    public void verifyRegistration(VerifyRegisterRequest request) {
        String pendingKey = pendingKey(request.getUserName());
        String attemptsKey = attemptsKey(request.getUserName());

        Object stored = redisTemplate.opsForValue().get(pendingKey);
        if (stored == null) {
            throw new InvalidRequestException("Code is expired or registration not found");
        }
        PendingRegistration pending = objectMapper.convertValue(stored, PendingRegistration.class);

        if (!pending.code().equals(request.getCode())) {
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, REGISTER_CODE_TTL);
            if (attempts != null && attempts >= MAX_CODE_ATTEMPTS) {
                redisTemplate.delete(pendingKey);
                redisTemplate.delete(attemptsKey);
            }
            throw new InvalidRequestException("Wrong code!");
        }

        userService.createWithDefaultRole(pending.userName(), pending.email(), pending.encodedPassword(), pending.phone());
        redisTemplate.delete(pendingKey);
        redisTemplate.delete(attemptsKey);
    }

    private String pendingKey(String userName) {
        return "register:pending:" + userName;
    }

    private String attemptsKey(String userName) {
        return "register:attempts:" + userName;
    }

    public record PendingRegistration(String userName, String email, String encodedPassword, String phone, String code) {
    }
}
