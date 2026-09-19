package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.jwtResponse;
import com.example.myBookShop.Service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/refresh-token")
@AllArgsConstructor
@CrossOrigin(origins ="*")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    //Create refresh Token

    @PostMapping("/{userId}")
    public ResponseEntity<?> create(@PathVariable int  userId){
        ApiRespone<String> res=new ApiRespone<>(
                true,refreshTokenService.create(userId).getToken(),"Created new refresh Token!", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
}
