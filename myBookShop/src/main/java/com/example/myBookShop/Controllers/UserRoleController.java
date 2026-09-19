package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.UserRoleResponse;
import com.example.myBookShop.Service.UserRoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/userRole")
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('ADMIN','USER','STAFF')")
@AllArgsConstructor
public class UserRoleController {
    private final UserRoleService userRoleService;

    @GetMapping
    ResponseEntity<?> getAll(){
        ApiRespone<List<UserRoleResponse>> res= new ApiRespone<>(
                true,userRoleService.getAll(),"Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
}
