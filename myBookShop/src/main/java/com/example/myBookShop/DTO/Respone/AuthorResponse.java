package com.example.myBookShop.DTO.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorResponse {
    private int id;
    private String name;
    private String country;
}
