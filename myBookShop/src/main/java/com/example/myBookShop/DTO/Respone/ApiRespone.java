package com.example.myBookShop.DTO.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRespone<T> {
    Boolean success;
    T data;
    String messages;
    LocalDateTime localDateTime;
}
