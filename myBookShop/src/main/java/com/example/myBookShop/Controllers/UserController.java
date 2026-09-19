package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateUserRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.PageResponse;
import com.example.myBookShop.DTO.Respone.UserSimpleResponse;
import com.example.myBookShop.Service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserRequest createUserRequest){
        ApiRespone<UserSimpleResponse> res=new ApiRespone<>(
                true,
                userService.create(createUserRequest),
                "Create User Successful!",
                LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
    @GetMapping
    public ResponseEntity<?> getAllSimple(
            @RequestParam(defaultValue = "0") int page , @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page,size);
        System.out.println("Đã vào đây");
        Page<UserSimpleResponse> listPage=userService.getAllSimpleUser(pageable);
        PageResponse<UserSimpleResponse> pageResponse = new PageResponse<>(
                listPage.getContent(),
                listPage.getNumber(),
                listPage.getSize(),
                listPage.getTotalElements(),
                listPage.getTotalPages(),
                listPage.isFirst(),
                listPage.isLast()
        );
        ApiRespone<PageResponse<UserSimpleResponse>> res= new ApiRespone<>(
                true,pageResponse,"Successful",LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }


}
