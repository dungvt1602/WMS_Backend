package com.project.wms.customer.controller;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.customer.dto.PartnerRequest;
import com.project.wms.customer.dto.PartnerResponse;
import com.project.wms.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Controller thêm đối tác
    @PostMapping
    public ResponseEntity<ApiResponse<PartnerResponse>> createPartner(@Valid @RequestBody PartnerRequest request) {
        PartnerResponse response = customerService.createPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Controller lấy danh sách tất cả đối tác theo dạng phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerResponse>>> getAllPartners(Pageable pageable) {
        Page<PartnerResponse> response = customerService.getAllPartners(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Controller lấy danh sách tất cả đối tác theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartnerById(@PathVariable Long id) {
        PartnerResponse response = customerService.getPartnerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Controller update đối tác
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody PartnerRequest request) {
        PartnerResponse response = customerService.updatePartner(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Controller delete (deactive) đối tác
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartner(@PathVariable Long id) {
        customerService.deletePartner(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
