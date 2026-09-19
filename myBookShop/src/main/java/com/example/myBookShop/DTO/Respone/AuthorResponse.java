package com.example.myBookShop.DTO.Respone;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorResponse {
    String name;
    String country;
}
