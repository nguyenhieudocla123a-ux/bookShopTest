package com.example.myBookShop.untils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GenerateCode {
    private static final int CODE_RANGE = 1_000_000;

    // Random có thể đoán được nếu biết seed, mã bảo mật phải dùng SecureRandom.
    private final SecureRandom random = new SecureRandom();

    /** nextInt(n) trả về 0 đến n-1, nên kết quả luôn nằm trong 000000 đến 999999. */
    public String genCode() {
        return String.format("%06d", random.nextInt(CODE_RANGE));
    }
}
