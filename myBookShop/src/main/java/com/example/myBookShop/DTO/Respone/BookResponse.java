package com.example.myBookShop.DTO.Respone;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    String title;
    BigDecimal price;
    int  quantity;
}
