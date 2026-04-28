package com.project.wms.common.exception;

import java.util.stream.Collectors;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.wms.common.response.ApiResponse;

/**
 * Global Exception Handler for the application.
 * Intercepts RuntimeExceptions and returns a standardized ApiResponse.
 */
@RestControllerAdvice // Marks this class as a global error filter for all Controllers
public class HandleRuntimeException {

    @ExceptionHandler(JwtAuthenticationException.class)
    public ApiResponse<String> handleJwtAuthenticationException(JwtAuthenticationException e) {
        return ApiResponse.error(401, e.getMessage());
    }

    /**
     * Catches RuntimeException and subclasses (like NullPointerException).
     * Returns a 500 error code with the exception message in ApiResponse format.
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<String> handleRuntimeException(RuntimeException e) {
        return ApiResponse.error(500, e.getMessage());
    }

    @ExceptionHandler(BussinessException.class)
    public ApiResponse<String> handleBussinessException(BussinessException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    // Xử lí lỗi @Valid khi trả response sai
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidationExceptions(
            MethodArgumentNotValidException e) {

        String error = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining(", ", "[", "]"));
        return ApiResponse.error(400, "Lỗi dữ liệu đầu vào: " + error);
    }

    @ExceptionHandler(ProductException.class)
    public ApiResponse<String> handleProductException(ProductException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(WarehouseException.class)
    public ApiResponse<String> handleWarehouseException(WarehouseException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(WarehouseZoneException.class)
    public ApiResponse<String> handleWarehouseZoneException(WarehouseZoneException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    
}
