package com.example.myBookShop.DTO.Respone;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BorrowRecordResponse {
    String userName;
    String bookName;
    LocalDate borrowDate;
    LocalDate returnDate;
    String status;
}
