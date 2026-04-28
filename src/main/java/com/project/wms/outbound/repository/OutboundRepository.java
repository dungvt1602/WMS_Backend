package com.project.wms.outbound.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.wms.common.enums.OrderStatus;
import com.project.wms.outbound.entity.OutboundOrder;

public interface OutboundRepository extends JpaRepository<OutboundOrder, Long> {

    // Tìm kiếm đơn xuất theo mã đơn hàng
    Optional<OutboundOrder> findByOrderCode(String orderCode);

    // Kiểm tra tồn tại mã đơn để tránh trùng lặp
    boolean existsByOrderCode(String orderCode);

    // Kiểm tra tồn tại mã yêu cầu để tránh trùng lặp
    Optional<OutboundOrder> findByRequestId(String requestId);

    // [PHASE 1 - IDEMPOTENCY] Atomic Update: Chỉ cập nhật nếu trạng thái hiện tại
    // đúng như kỳ vọng
    // Cập nhật trạng thái Đã giao hàng (SHIPPED)
    @Modifying(clearAutomatically = true)
    @Query(" UPDATE OutboundOrder o SET o.status=:new_status WHERE o.id= :id AND o.status=:old_status")
    int updateOrderStatus(@Param("id") Long id, @Param("new_status") OrderStatus newStatus,
            @Param("old_status") OrderStatus oldStatus);
}
