package com.project.wms.outbound.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.outbound.dto.OutboundRequest;
import com.project.wms.outbound.dto.OutboundResponse;
import com.project.wms.outbound.service.OutboundCompleteService;
import com.project.wms.outbound.service.OutboundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/outbounds")
@RequiredArgsConstructor
public class OutboundController {
    private final OutboundService outboundService;
    private final OutboundCompleteService outboundCompleteService;

    // Tạo phiếu xuất kho
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER','ROLE_USER')")
    public ResponseEntity<ApiResponse<OutboundResponse>> createOrder(@Valid @RequestBody OutboundRequest request) {
        OutboundResponse outboundResponse = outboundService.createOrder(request);
        // Trả về 201 Created kèm dữ liệu đã bọc trong ApiResponse
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(outboundResponse));
    }

    // Hoàn thành phiếu xuất kho
    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<OutboundResponse>> completeOrder(
            @PathVariable Long orderId, @RequestBody Long userId) {
        OutboundResponse response = outboundCompleteService.completeOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Hủy phiếu xuất kho
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<OutboundResponse>> cancelOrder(@PathVariable Long orderId) {
        OutboundResponse response = outboundService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
