package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String userName;
    // BCrypt chỉ dùng 72 byte đầu nên giới hạn độ dài tối đa.
    @NotBlank
    @Size(min = 8, max = 72)
    private String passWord;
    @Size(max = 20)
    private String phone;
    @NotBlank
    @Email
    private String email;
}
