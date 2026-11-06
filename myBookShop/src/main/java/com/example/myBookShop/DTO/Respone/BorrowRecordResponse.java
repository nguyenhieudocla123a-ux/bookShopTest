package com.example.myBookShop.DTO.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordResponse {
    private int id;
    private String userName;
    private String bookName;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate returnedDate;
    private String status;
}
