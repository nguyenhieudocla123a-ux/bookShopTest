package com.example.myBookShop.DTO.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Không có userId: người mượn luôn lấy từ token, client không được tự khai.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBorrowRecordRequest {
    @NotNull
    private Integer bookId;
    @NotNull
    @Min(1)
    @Max(60)
    private Integer borrowDays;
}
