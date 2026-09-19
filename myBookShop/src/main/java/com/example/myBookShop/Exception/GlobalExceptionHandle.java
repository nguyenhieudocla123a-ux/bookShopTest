package com.example.myBookShop.Exception;

import com.example.myBookShop.DTO.Respone.ApiRespone;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(ConflictDataException.class)
    public ResponseEntity<?> handleConflict(ConflictDataException exp){
        return ResponseEntity.status(HttpStatus.CONFLICT).body( new ApiRespone<>(
                false,
                null,
                exp.getMessage(),
                LocalDateTime.now()
        ));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException exp){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiRespone<>(
                false,
                null,
                exp.getMessage(),
                LocalDateTime.now()
        ));
    }

}
