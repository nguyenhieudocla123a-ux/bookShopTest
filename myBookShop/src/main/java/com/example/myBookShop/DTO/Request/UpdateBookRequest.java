package com.example.myBookShop.DTO.Request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;

@Data
@RequiredArgsConstructor
public class UpdateBookRequest {
    private int  id;
    private BigDecimal price;
    private String title;
    private int quantity;
}
