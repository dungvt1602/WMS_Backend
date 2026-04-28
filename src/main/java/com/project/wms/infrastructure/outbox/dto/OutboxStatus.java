package com.project.wms.infrastructure.outbox.dto;

public enum OutboxStatus {
    PENDING, // chờ gửi
    SENT, // đã gửi thành công
    FAILED, // gửi lỗi (vẫn retry nếu retryCount < 5)
    DEAD // retry quá 5 lần → cần xử lý thủ công / alert
}