package com.project.wms.outbound.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.outbound.entity.OutboundOrder;

public interface OutboundRepository extends JpaRepository<OutboundOrder, Long> {

    // Tìm kiếm đơn xuất theo mã đơn hàng
    Optional<OutboundOrder> findByOrderCode(String orderCode);

    // Kiểm tra tồn tại mã đơn để tránh trùng lặp
    boolean existsByOrderCode(String orderCode);
}
