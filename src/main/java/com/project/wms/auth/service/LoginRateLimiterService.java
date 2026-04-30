package com.project.wms.auth.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.wms.common.exception.LoginRateLimitExceededException;

@Service
public class LoginRateLimiterService {

    private static final class AttemptWindow {
        long windowStartSec;
        int count;

        AttemptWindow(long windowStartSec, int count) {
            this.windowStartSec = windowStartSec;
            this.count = count;
        }
    }

    @Value("${security.rate-limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.rate-limit.login.window-seconds:60}")
    private long windowSeconds;

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void checkRateLimit(String username, String clientIp) {
        long now = Instant.now().getEpochSecond();
        String key = buildRateLimitKey(username, clientIp);

        AttemptWindow window = attempts.computeIfAbsent(key, k -> new AttemptWindow(now, 0));

        synchronized (window) {
            if (now - window.windowStartSec >= windowSeconds) {
                window.windowStartSec = now;
                window.count = 0;
            }

            window.count++;
            if (window.count > maxAttempts) {
                throw new LoginRateLimitExceededException("Ban gui dang nhap qua nhieu lan. Vui long thu lai sau.");
            }
        }
    }

    public String buildRateLimitKey(String username, String clientIp) {
        String normalizedUser = (username == null || username.isBlank())
                ? "unknown-user" : username.trim().toLowerCase();
        String normalizedIp = (clientIp == null || clientIp.isBlank())
                ? "unknown-ip" : clientIp.trim().toLowerCase();
        return normalizedUser + "@" + normalizedIp;
    }

    public void resetRateLimit(String username, String clientIp) {
        String key = buildRateLimitKey(username, clientIp);
        attempts.remove(key);
    }
}
