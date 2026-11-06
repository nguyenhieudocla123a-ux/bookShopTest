package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateBorrowRecordRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.BorrowRecordResponse;
import com.example.myBookShop.DTO.Respone.PageResponse;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Service.BorrowRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    @PostMapping
    public ResponseEntity<ApiRespone<BorrowRecordResponse>> create(@Valid @RequestBody CreateBorrowRecordRequest request,
                                                                   @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiRespone<>(
                true, borrowRecordService.create(request, currentUser.getId()), "Successfully", LocalDateTime.now()));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ApiRespone<BorrowRecordResponse>> returnBook(@PathVariable int id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, borrowRecordService.returnBook(id, currentUser), "Successfully", LocalDateTime.now()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiRespone<PageResponse<BorrowRecordResponse>>> getMine(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(new ApiRespone<>(
                true,
                PageResponse.from(borrowRecordService.getByUser(currentUser.getId(), newestFirst(page, size))),
                "Successfully",
                LocalDateTime.now()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiRespone<PageResponse<BorrowRecordResponse>>> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, PageResponse.from(borrowRecordService.getAll(newestFirst(page, size))), "Successfully", LocalDateTime.now()));
    }

    private PageRequest newestFirst(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
