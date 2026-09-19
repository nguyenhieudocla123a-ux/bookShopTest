package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.RoleRespone;
import com.example.myBookShop.Service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasAnyRole;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/role")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    private final  RoleService roleService;

    @GetMapping
    public  ResponseEntity<?> getAll(){
        ApiRespone<List<RoleRespone>> res= new ApiRespone<>(
                true,roleService.getAll(),"Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }


}
