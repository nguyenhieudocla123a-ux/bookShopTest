package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateBorrowRecordRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.BorrowRecordResponse;
import com.example.myBookShop.Service.BorrowRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor

public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateBorrowRecordRequest request){
        ApiRespone<BorrowRecordResponse> res = new ApiRespone<>(
                true,borrowRecordService.create(request),"Successfully", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
}
