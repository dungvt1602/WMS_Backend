package com.project.wms.outbound.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.outbound.dto.OutboundRequest;
import com.project.wms.outbound.dto.OutboundResponse;
import com.project.wms.outbound.service.OutboundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/outbounds")
@RequiredArgsConstructor
public class OutboundController {
    private final OutboundService outboundService;

    // Tạo phiếu xuất kho
    @PostMapping
    public ResponseEntity<ApiResponse<OutboundResponse>> createOrder(@Valid @RequestBody OutboundRequest request) {
        OutboundResponse outboundResponse = outboundService.createOrder(request);
        // Trả về 201 Created kèm dữ liệu đã bọc trong ApiResponse
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(outboundResponse));
    }

    // Hoàn thành phiếu xuất kho
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<OutboundResponse>> completeOrder(
            @PathVariable Long orderId, @RequestBody Long zoneId) {
        OutboundResponse response = outboundService.completeOrder(orderId, zoneId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
