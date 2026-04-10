package com.project.wms.outbound.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.outbound.entity.OutboundOrder;
import com.project.wms.outbound.entity.OutboundOrderItems;

public interface OutboundItemRepository extends JpaRepository<OutboundOrderItems, Long> {

    // Tìm tất cả item theo đơn xuất kho
    List<OutboundOrderItems> findByOutboundOrder(OutboundOrder outboundOrder);

    // Xóa toàn bộ item của một đơn (khi hủy đơn)
    void deleteByOutboundOrder(OutboundOrder outboundOrder);
}
