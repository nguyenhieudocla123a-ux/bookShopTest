package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateUserRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.PageResponse;
import com.example.myBookShop.DTO.Respone.UserSimpleResponse;
import com.example.myBookShop.Service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiRespone<UserSimpleResponse>> create(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiRespone<>(
                true, userService.create(createUserRequest), "Create User Successful!", LocalDateTime.now()));
    }

    @GetMapping
    public ResponseEntity<ApiRespone<PageResponse<UserSimpleResponse>>> getAllSimple(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(new ApiRespone<>(
                true, PageResponse.from(userService.getAllSimpleUser(PageRequest.of(page, size))), "Successful", LocalDateTime.now()));
    }
}
