package com.project.wms.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project.wms.auth.dto.AuthResponse;
import com.project.wms.auth.dto.LoginRequest;
import com.project.wms.auth.dto.RegisterRequest;
import com.project.wms.auth.service.AuthService;
import com.project.wms.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String hello() {
        return "Hello";
    }

    // hàm đăng ký sử dụng responseEntity
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        var response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));

    }

    // hàm đăng nhập sử dụng responseEntity
    @PostMapping("/login")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        var response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));

    }

}
