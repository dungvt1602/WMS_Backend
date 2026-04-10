package com.project.wms.inbound.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.inbound.entity.InboundOrder;
import com.project.wms.inbound.entity.InboundOrderItems;

public interface InboundItemsRepository extends JpaRepository<InboundOrderItems, Long> {

    // tìm kiếm tất cả item theo đơn nhập
    List<InboundOrderItems> findByInboundOrder(InboundOrder inboundOrder);

    // xóa các item trong đơn nhập
    void deleteByInboundOrder(InboundOrder inboundOrder);
}
