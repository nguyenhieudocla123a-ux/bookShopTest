package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateBookRequest;
import com.example.myBookShop.DTO.Request.UpdateBookRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.DTO.Respone.BookViewResponse;
import com.example.myBookShop.DTO.Respone.PageResponse;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Mọi API đều cần đăng nhập (SecurityConfig). USER được đọc, chỉ ADMIN/STAFF được thay đổi dữ liệu.
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiRespone<BookResponse>> create(@Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiRespone<>(
                true, bookService.create(request), "Successfully", LocalDateTime.now()));
    }

    // PageRequest đếm trang từ 0, mặc định 1 sẽ bỏ qua trang đầu tiên.
    @GetMapping
    public ResponseEntity<ApiRespone<PageResponse<BookResponse>>> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, PageResponse.from(bookService.getAll(PageRequest.of(page, size))), "Successfully", LocalDateTime.now()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiRespone<BookResponse>> update(@PathVariable int id, @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, bookService.update(id, request), "Successfully", LocalDateTime.now()));
    }

    // Các đường dẫn cố định như /recent được Spring ưu tiên hơn /{id}.
    @GetMapping("/recent")
    public ResponseEntity<ApiRespone<List<Object>>> getRecentBooks(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, bookService.getRecentBooks(currentUser.getId()), "Successfully", LocalDateTime.now()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiRespone<BookResponse>> getBook(@PathVariable int id) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, bookService.getBook(id), "Successfully", LocalDateTime.now()));
    }

    // Lấy user từ token thay vì nhận userId trên URL, để không ghi được lịch sử của người khác.
    @GetMapping("/search/{bookId}")
    public ResponseEntity<ApiRespone<BookResponse>> searchBook(@PathVariable int bookId, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, bookService.viewBook(bookId, currentUser.getId()), "Successfully", LocalDateTime.now()));
    }

    @PostMapping("/book-view/{bookId}/{score}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiRespone<Void>> setBookView(@PathVariable int bookId, @PathVariable @PositiveOrZero int score) {
        bookService.setViews(bookId, score);
        return ResponseEntity.ok(new ApiRespone<>(
                true, null, "Set count of views for this book successfully", LocalDateTime.now()));
    }

    @GetMapping("/top-views/{topCount}")
    public ResponseEntity<ApiRespone<List<BookViewResponse>>> topViews(@PathVariable @Min(1) @Max(100) int topCount) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, bookService.topViews(topCount), "Successfully", LocalDateTime.now()));
    }
}
