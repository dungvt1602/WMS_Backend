package com.project.wms.common.exception;

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

    @ExceptionHandler(UsernameNotFound.class)
    public ApiResponse<String> handleUsernameNotFoundException(UsernameNotFound e) {
        return ApiResponse.error(404, "Không tìm thấy Username này");
    }
}
