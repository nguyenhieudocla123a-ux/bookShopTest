package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.UserRoleResponse;
import com.example.myBookShop.Service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

// Danh sách user và quyền là dữ liệu quản trị, không để USER xem được.
@RestController
@RequestMapping("/api/userRole")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserRoleController {
    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<ApiRespone<List<UserRoleResponse>>> getAll() {
        return ResponseEntity.ok(new ApiRespone<>(
                true, userRoleService.getAll(), "Successful", LocalDateTime.now()));
    }
}
