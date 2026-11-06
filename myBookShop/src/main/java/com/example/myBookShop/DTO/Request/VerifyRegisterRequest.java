package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRegisterRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String code;
}
