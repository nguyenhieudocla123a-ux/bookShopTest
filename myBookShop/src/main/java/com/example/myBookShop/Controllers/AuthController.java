package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.LoginRequest;
import com.example.myBookShop.DTO.Request.LogoutRequest;
import com.example.myBookShop.DTO.Request.RefreshTokenRequest;
import com.example.myBookShop.DTO.Request.RegisterRequest;
import com.example.myBookShop.DTO.Request.VerifyRegisterRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.TokenResponse;
import com.example.myBookShop.Service.AuthService;
import com.example.myBookShop.Service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiRespone<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, authService.login(request), "Login Successful", LocalDateTime.now()));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiRespone<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.startRegistration(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiRespone<>(
                true, null, "Verification code created", LocalDateTime.now()));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiRespone<Void>> verifyRegistration(@Valid @RequestBody VerifyRegisterRequest request) {
        authService.verifyRegistration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiRespone<>(
                true, null, "Sign up Successful!", LocalDateTime.now()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiRespone<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, refreshTokenService.rotate(request.getRefreshToken()), "Token refreshed", LocalDateTime.now()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiRespone<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(new ApiRespone<>(true, null, "Logout Successful", LocalDateTime.now()));
    }
}
