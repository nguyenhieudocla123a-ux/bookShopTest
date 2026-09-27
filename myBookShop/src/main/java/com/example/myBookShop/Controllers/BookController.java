package com.example.myBookShop.Controllers;


import com.example.myBookShop.DTO.Request.CreateBookRequest;
import com.example.myBookShop.DTO.Request.UpdateBookRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.DTO.Respone.PageResponse;
import com.example.myBookShop.Service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/book")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateBookRequest request){
        ApiRespone<BookResponse> res = new ApiRespone<>(
                true,bookService.create(request),"Successfully", LocalDateTime.now()

        );
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "10") int size , @RequestParam(defaultValue = "1") int page){
        Pageable pageable= PageRequest.of(page,size);
        Page<BookResponse> pageRes=bookService.getAll(pageable);

        PageResponse<BookResponse> pageResponse=new PageResponse<>(
                pageRes.getContent(),
                pageRes.getNumber(),
                pageRes.getSize(),
                pageRes.getTotalElements(),
                pageRes.getTotalPages(),
                pageRes.isFirst(),
                pageRes.isLast()

        );
        ApiRespone<PageResponse<?>> res = new ApiRespone<>(
                true,pageResponse,"Successfully",LocalDateTime.now()
        );
        return ResponseEntity.ok(res);


    }

    //TODO Updating book ( title , quantity ,price.. )
    @PostMapping("/book-update")
    public ResponseEntity<?> update(@RequestBody  UpdateBookRequest request){
        ApiRespone<BookResponse> res = new ApiRespone<>(
                true,bookService.update(request),"Successfully",LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }

}
