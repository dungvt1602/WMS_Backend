package com.project.wms.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project.wms.auth.dto.AuthResponse;
import com.project.wms.auth.dto.LogoutRequest;
import com.project.wms.auth.dto.LoginRequest;
import com.project.wms.auth.dto.RegisterRequest;
import com.project.wms.auth.dto.TokenPairResponse;
import com.project.wms.auth.dto.TokenRefreshRequest;
import com.project.wms.auth.service.AuthService;
import com.project.wms.auth.service.LoginRateLimiterService;
import com.project.wms.auth.service.RefreshTokenService;
import com.project.wms.common.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimiterService loginRateLimiterService;

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
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest httpRequest) {
        loginRateLimiterService.checkRateLimit(request.username(), httpRequest.getRemoteAddr());

        var response = authService.login(request);
        loginRateLimiterService.resetRateLimit(request.username(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));

    }

    //Hàm này dùng để refreshToken
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPairResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request,
                                                                  HttpServletRequest httpRequest) {
        var response = refreshTokenService.refresh(request, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //Hàm này dùng để logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request,
                                                    HttpServletRequest httpRequest) {
        refreshTokenService.logout(request, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

}
