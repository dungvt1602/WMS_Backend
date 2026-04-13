package com.project.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication // Đánh dấu đây là ứng dụng Spring Boot
@EnableCaching // Cho phép sử dụng tính năng Cache (chạy lần 1 lấy DB, lần 2 lấy Cache)
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args); // Khởi động ứng dụng
    }

}
