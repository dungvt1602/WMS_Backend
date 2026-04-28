package com.project.wms.inbound.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.wms.common.enums.OrderStatus;
import com.project.wms.inbound.entity.InboundOrder;

public interface InboundRepository extends JpaRepository<InboundOrder, Long> {

    // Tìm kiếm đơn nhập theo số hiệu đơn hàng (Ví dụ: PO-2026-001)
    Optional<InboundOrder> findByOrderNumber(String orderNumber);

    // Kiểm tra tồn tại số hiệu đơn để tránh trùng lặp chứng từ
    boolean existsByOrderNumber(String orderNumber);

    // [PHASE 1 - IDEMPOTENCY] Atomic Update: Chỉ cập nhật nếu trạng thái hiện tại
    // đúng như kỳ vọng
    // Cập nhật trạng thái Đã nhận hàng (RECEIVE_COMPLETE)
    @Modifying
    @Query(" UPDATE InboundOrder o SET o.status=:new_status WHERE o.id= :id AND o.status=:old_status")
    int updateOrderStatus(@Param("id") Long id, @Param("new_status") OrderStatus newStatus,
            @Param("old_status") OrderStatus oldStatus);
}
