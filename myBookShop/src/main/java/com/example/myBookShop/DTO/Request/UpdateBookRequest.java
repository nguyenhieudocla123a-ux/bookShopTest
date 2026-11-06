package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBookRequest {
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    @NotBlank
    private String title;
    @PositiveOrZero
    private int quantity;
}
