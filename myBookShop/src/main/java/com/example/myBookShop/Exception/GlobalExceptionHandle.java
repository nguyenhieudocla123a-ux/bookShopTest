package com.example.myBookShop.Exception;

import com.example.myBookShop.DTO.Respone.ApiRespone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(ConflictDataException.class)
    public ResponseEntity<ApiRespone<Void>> handleConflict(ConflictDataException exp) {
        return error(HttpStatus.CONFLICT, exp.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiRespone<Void>> handleNotFound(ResourceNotFoundException exp) {
        return error(HttpStatus.NOT_FOUND, exp.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiRespone<Void>> handleNoResource(NoResourceFoundException exp) {
        return error(HttpStatus.NOT_FOUND, "API not found");
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiRespone<Void>> handleInvalidRequest(InvalidRequestException exp) {
        return error(HttpStatus.BAD_REQUEST, exp.getMessage());
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiRespone<Void>> handleToken(TokenException exp) {
        return error(HttpStatus.UNAUTHORIZED, exp.getMessage());
    }

    /** Lỗi @Valid ở @RequestBody: trả từng trường sai để client hiển thị. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiRespone<Map<String, String>>> handleValidation(MethodArgumentNotValidException exp) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exp.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ApiRespone<>(false, errors, "Validation failed", LocalDateTime.now()));
    }

    /** Lỗi ràng buộc ở @RequestParam và @PathVariable, ví dụ page = -1. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiRespone<Void>> handleParamValidation(HandlerMethodValidationException exp) {
        return error(HttpStatus.BAD_REQUEST, "Request parameters are invalid");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiRespone<Void>> handleBadInput(Exception exp) {
        return error(HttpStatus.BAD_REQUEST, "Request data is invalid");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiRespone<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exp) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not supported");
    }

    // Dùng chung một thông báo cho sai username và sai mật khẩu để không lộ username nào tồn tại.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiRespone<Void>> handleAuthentication(AuthenticationException exp) {
        String message = exp instanceof DisabledException ? "Account is disabled" : "Username or password is incorrect";
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    // Phải khai báo riêng, nếu không handler Exception bên dưới sẽ biến lỗi 403 thành 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiRespone<Void>> handleAccessDenied(AccessDeniedException exp) {
        return error(HttpStatus.FORBIDDEN, "You do not have permission");
    }

    // Ví dụ hai request đăng ký cùng username vượt qua bước kiểm tra và đụng UNIQUE của DB.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiRespone<Void>> handleDataIntegrity(DataIntegrityViolationException exp) {
        return error(HttpStatus.CONFLICT, "Data conflicts with existing data");
    }

    /** Lỗi không lường trước: ghi log đầy đủ cho lập trình viên, không trả chi tiết nội bộ cho client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiRespone<Void>> handleUnexpected(Exception exp) {
        log.error("Unexpected error", exp);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ApiRespone<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiRespone<>(false, null, message, LocalDateTime.now()));
    }
}
