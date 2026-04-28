package com.project.wms.common.exception;

import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
@Setter
public class AccessDeniedException extends RuntimeException {
    private Long userId;
    private Long warehouseId;
    private String code;

    public AccessDeniedException(String code, Long userId, Long warehouseId) {
        super(String.format("Người dùng %d không có quyền %s tại kho %d",
                userId,
                code, warehouseId));
        this.userId = userId;
        this.warehouseId = warehouseId;
        this.code = code;
    }


}
