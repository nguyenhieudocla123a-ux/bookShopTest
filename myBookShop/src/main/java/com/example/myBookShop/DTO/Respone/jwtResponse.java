package com.example.myBookShop.DTO.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class jwtResponse {
    private String token;
    private String username;
    private String email;
    private List<String> roles;
}
