package com.project.wms.auth.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.project.wms.auth.entity.User;

@Component
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        throw new RuntimeException("Không lấy được user");
    }
}