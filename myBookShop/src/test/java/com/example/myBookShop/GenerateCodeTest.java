package com.example.myBookShop;

import com.example.myBookShop.untils.GenerateCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateCodeTest {

    @Test
    @DisplayName("Lỗi cũ: mã có thể ra -00001 và chỉ có 5 chữ số có nghĩa")
    void codeIsAlwaysSixDigits() {
        GenerateCode generateCode = new GenerateCode();
        boolean sawSixSignificantDigits = false;
        for (int i = 0; i < 100_000; i++) {
            String code = generateCode.genCode();
            assertThat(code).matches("\\d{6}");
            sawSixSignificantDigits |= code.charAt(0) != '0';
        }
        assertThat(sawSixSignificantDigits).isTrue();
    }
}
