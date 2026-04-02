package com.project.wms.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.wms.auth.dto.AuthResponse;
import com.project.wms.auth.dto.LoginRequest;
import com.project.wms.auth.dto.RegisterRequest;
import com.project.wms.auth.service.AuthService;
import com.project.wms.common.response.ApiResponse;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // hàm đăng ký sử dụng responseEntity
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        var response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));

    }

    // hàm đăng nhập sử dụng responseEntity
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        var response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));

    }

}
