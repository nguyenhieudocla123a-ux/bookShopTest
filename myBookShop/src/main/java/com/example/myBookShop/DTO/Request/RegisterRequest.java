package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    String userName;
    @NotBlank
    String passWord;
    String phone;
    String email;
}
