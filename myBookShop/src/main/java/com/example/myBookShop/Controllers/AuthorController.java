package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.CreateAuthorRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.AuthorResponse;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Service.AuthorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
@RequestMapping("/api/author")
@CrossOrigin("*")
@AllArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    ResponseEntity<?> getAll(){
        ApiRespone<List<AuthorResponse>> res= new ApiRespone<>(
                true,authorService.getAll(),"Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping
    ResponseEntity<?> create(@Valid @RequestBody CreateAuthorRequest request){

        ApiRespone<AuthorResponse> res= new ApiRespone<>(
                true,authorService.create(request),"Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
    @GetMapping("/book/{id}")
    ResponseEntity<?> getAllBook(@PathVariable int id){

        ApiRespone<List<BookResponse>> res= new ApiRespone<>(
                true,authorService.findBook(id),"Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable int id){
        authorService.delete(id);
        ApiRespone<Void> res= new ApiRespone<>(
                true,null,"Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
}
