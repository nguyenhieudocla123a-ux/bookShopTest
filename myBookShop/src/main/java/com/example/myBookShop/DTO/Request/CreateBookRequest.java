package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.transform.Source;
import java.math.BigDecimal;

// @Valid ở controller chỉ có tác dụng khi DTO có các ràng buộc như dưới đây.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest  {
    @NotBlank
    private String title;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    @PositiveOrZero
    private int quantity;
    @NotBlank
    private String authorName;
}
