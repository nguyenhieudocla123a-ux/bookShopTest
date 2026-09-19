package com.example.myBookShop.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateBorrowRecordRequest {
    int userId;
    int bookId;
}
