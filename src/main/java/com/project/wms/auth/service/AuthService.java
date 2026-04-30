package com.project.wms.auth.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.wms.auth.dto.AuthResponse;
import com.project.wms.auth.dto.LoginRequest;
import com.project.wms.auth.dto.RegisterRequest;
import com.project.wms.auth.entity.Role;
import com.project.wms.auth.entity.User;
import com.project.wms.auth.repository.RoleRepository;
import com.project.wms.auth.repository.UserRepository;
import com.project.wms.auth.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    // ham dang ky tai khoan cua nguoi dung
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        Role userRole = roleRepository.findByName("ROLE_VIEWER")
                .or(() -> roleRepository.findByName("ROLE_USER"))
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(userRole)); // danh sach role set.of = Set<Role>

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername());
    }

    // ham dang nhap tai khoan cua nguoi dung
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // xac thuc nguoi dung2 dung authencationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        var user = userRepository.findByUsername(request.username()).orElseThrow();

        var token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername());
    }

}
