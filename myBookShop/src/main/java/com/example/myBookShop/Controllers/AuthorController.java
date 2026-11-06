package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateAuthorRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.AuthorResponse;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// USER chỉ được đọc. Trước đây @PreAuthorize đặt ở cấp class nên USER tạo và xoá được tác giả.
@RestController
@RequestMapping("/api/author")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<ApiRespone<List<AuthorResponse>>> getAll() {
        return ResponseEntity.ok(new ApiRespone<>(
                true, authorService.getAll(), "Successful", LocalDateTime.now()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiRespone<AuthorResponse>> create(@Valid @RequestBody CreateAuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiRespone<>(
                true, authorService.create(request), "Successful", LocalDateTime.now()));
    }

    @GetMapping("/book/{id}")
    public ResponseEntity<ApiRespone<List<BookResponse>>> getAllBook(@PathVariable int id) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, authorService.findBook(id), "Successful", LocalDateTime.now()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiRespone<Void>> delete(@PathVariable int id) {
        authorService.delete(id);
        return ResponseEntity.ok(new ApiRespone<>(true, null, "Successful", LocalDateTime.now()));
    }
}
