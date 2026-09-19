package com.example.myBookShop.DTO.Request;

import com.example.myBookShop.Entity.Author;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CreateBookRequest {
    String title;

    BigDecimal price;

    int  quantity;
    String  authorName;
}
