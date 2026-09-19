package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAuthorRequest {

    @NotBlank
    String name;
    @NotBlank
    String country;
}
