package com.project.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication // Đánh dấu đây là ứng dụng Spring Boot
@EnableCaching // Cho phép sử dụng tính năng Cache (chạy lần 1 lấy DB, lần 2 lấy Cache)
@EnableAsync
@EnableRetry // Cho phep gui lai tin nhan
@EnableMethodSecurity  //dùng để check role tổng dđể bypass ADMIN
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args); // Khởi động ứng dụng
    }

}
