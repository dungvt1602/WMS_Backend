package com.project.wms.inbound.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.inbound.entity.InboundOrder;

public interface InboundRepository extends JpaRepository<InboundOrder, Long> {

    // Tìm kiếm đơn nhập theo số hiệu đơn hàng (Ví dụ: PO-2026-001)
    Optional<InboundOrder> findByOrderNumber(String orderNumber);

    // Kiểm tra tồn tại số hiệu đơn để tránh trùng lặp chứng từ
    boolean existsByOrderNumber(String orderNumber);
}
