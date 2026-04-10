package com.project.wms.inbound.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.inbound.dto.InboundRequest;
import com.project.wms.inbound.dto.InboundResponse;
import com.project.wms.inbound.service.InboundService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/v1/inbound")
@RequiredArgsConstructor
public class InboundController {
    private final InboundService inboundService;

    // tạo phiếu nhập kho
    @PostMapping
    public ResponseEntity<ApiResponse<InboundResponse>> createOrder(@Valid @RequestBody InboundRequest request) {
        InboundResponse inboundResponse = inboundService.createOrder(request);
        // Trả về 201 Created kèm dữ liệu đã bọc trong ApiResponse
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(inboundResponse));
    }

    // Hoan thanh phieu nhap kho
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<InboundResponse>> completeOrder(
            @PathVariable Long orderId) {
        InboundResponse response = inboundService.completeOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
