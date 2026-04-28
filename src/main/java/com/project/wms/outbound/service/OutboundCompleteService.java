package com.project.wms.outbound.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.project.wms.auth.dto.PermissionCode;
import com.project.wms.auth.repository.PermissionWarehouseRepository;
import com.project.wms.auth.service.PermissionWarehouseService;
import com.project.wms.outbound.dto.OutboundRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.common.enums.OrderStatus;
import com.project.wms.infrastructure.outbox.service.OutboxInventoryService;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.kafka.InventoryEvent;
import com.project.wms.inventory.service.InventoryRedisService;
import com.project.wms.inventory.service.InventoryService;
import com.project.wms.outbound.dto.OutboundItemRequest;
import com.project.wms.outbound.dto.OutboundResponse;
import com.project.wms.outbound.dto.ReservedItem;
import com.project.wms.outbound.entity.OutboundOrder;
import com.project.wms.outbound.entity.OutboundOrderItems;
import com.project.wms.outbound.repository.OutboundRepository;
import com.project.wms.product.entity.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundCompleteService {

    private final OutboundRepository outboundRepository;
    private final InventoryService inventoryService;
    private final InventoryRedisService inventoryRedisService;
    private final OutboxInventoryService outboxInventoryService;
    private final PermissionWarehouseService permissionWarehouseService;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC ENTRY — không @Transactional, tránh giữ DB connection khi gọi Redis
    // ─────────────────────────────────────────────────────────────────────────

    public OutboundResponse completeOrder(Long orderId, Long userId) {

        // Phase 1: Validate ngoài transaction (1 query, không giữ connection)
        OutboundOrder order = outboundRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Order có ID: " + orderId));

        //Check permision
        checkPermssion(order, userId);

        // FIX BUG 3: so sánh enum đúng cách, không dùng string "PENDING"
        if (order.getStatus() != OrderStatus.PENDING) {
            log.error("Đơn hàng {} đang ở trạng thái {}, không thể xuất kho",
                    orderId, order.getStatus());
            throw new RuntimeException("Đơn hàng đã được phê duyệt rồi hoặc đã bị hủy");
        }


        // Phase 1: Reserve inventory — ngoài transaction
        List<ReservedItem> reservedItems = reserveAllItems(order, userId);

        // Phase 2: Commit DB trong transaction ngắn
        // FIX BUG 1: commitOrder phải là public để Spring AOP proxy @Transactional đúng
        // private method + @Transactional = không có tác dụng gì
        return commitOrder(order, reservedItems, userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHASE 1 — Reserve inventory (ngoài transaction)
    // ─────────────────────────────────────────────────────────────────────────

    private List<ReservedItem> reserveAllItems(OutboundOrder order, Long userId) {
        List<ReservedItem> reservedItems = new ArrayList<>();

        for (OutboundOrderItems item : order.getOutboundOrderItems()) {
            try {
                reserveOneItem(item, order, userId, reservedItems);
            } catch (Exception e) {
                // FIX BUG 3: item fail giữa chừng → rollback tất cả item đã reserve trước đó
                log.warn("[RESERVE FAILED] productId={}, rolling back {} reserved items",
                        item.getProduct().getId(), reservedItems.size());
                rollbackReservedItems(reservedItems, order, userId);
                throw e;
            }
        }

        return reservedItems;
    }

    private void reserveOneItem(
            OutboundOrderItems item,
            OutboundOrder order,
            Long userId,
            List<ReservedItem> reservedItems) {
        Long productId = item.getProduct().getId();
        Long warehouseId = item.getWarehouse().getId();
        Long zoneId = item.getZone().getId();
        int quantity = item.getQuantity();

        if (hotItem(item.getProduct())) {
            // Hot item: trừ Redis atomic
            Boolean ok = inventoryRedisService.decreaseStockAtomic(
                    warehouseId, zoneId, productId, quantity, order.getOrderCode());

            if (!ok) {
                log.error("[RESERVE] Hết hàng hoặc đã trừ trước: orderCode={} productId={}",
                        order.getOrderCode(), productId);
                throw new RuntimeException(
                        "Hết hàng hoặc bạn đã gửi lệnh trừ từ trước: " + order.getOrderCode());
            }

            reservedItems.add(ReservedItem.hot(productId, zoneId, warehouseId, quantity));
            log.debug("[RESERVE] Hot item trừ Redis thành công: productId={} qty={}", productId, quantity);

        } else {
            // Cold item: trừ DB với lock
            InventoryRequest req = new InventoryRequest(
                    warehouseId, zoneId, productId, quantity, userId, order.getOrderCode());
            try {
                inventoryService.removeStock(req, order.getOrderCode());
                reservedItems.add(ReservedItem.cold(productId, zoneId, warehouseId, quantity));
                log.debug("[RESERVE] Cold item trừ DB thành công: productId={} qty={}", productId, quantity);
            } catch (Exception e) {
                log.error("[RESERVE] Lỗi trừ DB: productId={} error={}", productId, e.getMessage());
                throw new RuntimeException("Lỗi khi trừ tồn kho ở DB: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHASE 2 — Commit DB (transaction ngắn: 1 UPDATE + INSERT outbox)
    //
    // FIX BUG 1: đổi private → public để Spring AOP proxy @Transactional đúng.
    // Khi completeOrder() gọi this.commitOrder() nội bộ, Spring không thể intercept
    // → @Transactional bị bỏ qua hoàn toàn dù code trông đúng.
    // public method được Spring proxy → transaction hoạt động đúng.
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundResponse commitOrder(
            OutboundOrder order,
            List<ReservedItem> reservedItems,
            Long userId) {
        // Optimistic lock: chỉ update nếu vẫn còn PENDING
        int updated = outboundRepository.updateOrderStatus(
                order.getId(), OrderStatus.COMPLETED, OrderStatus.PENDING);

        if (updated == 0) {
            // Race condition: request khác đã xử lý trước → rollback inventory
            log.error("[COMMIT] Race condition orderId={}, rolling back inventory", order.getId());
            rollbackReservedItems(reservedItems, order, userId);
            throw new RuntimeException(
                    "Không thể update order " + order.getId() + " — đã được xử lý bởi request khác");
        }

        // FIX BUG 2: lưu outbox event cho TẤT CẢ item (hot + cold)
        // Hot item → OutboxDispatcher gửi Kafka → consumer trừ hàng
        // Cold item → đã trừ DB ở Phase 1, event để audit/trace
        reservedItems.forEach(item -> {
            String movementType = item.isHot() ? "OUTBOUND_HOT" : "OUTBOUND_COLD";
            InventoryEvent event = new InventoryEvent(
                    item.warehouseId(),
                    item.zoneId(),
                    item.productId(),
                    item.quantity(),
                    movementType,
                    userId,
                    "",
                    order.getOrderCode());
            outboxInventoryService.saveInventoryOutboundEvent(event);
        });

        log.info("[COMMIT] orderId={} completed, {} outbox events saved",
                order.getId(), reservedItems.size());

        // Reload để trả về status COMPLETED (không dùng object cũ)
        OutboundOrder current = outboundRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy order: " + order.getId()));
        return mapToResponse(current);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rollback reserved items (best-effort)
    //
    // FIX BUG 2: rollback cả hot (Redis) lẫn cold (DB)
    // Không throw trong loop — tiếp tục rollback item còn lại dù 1 item fail
    // ─────────────────────────────────────────────────────────────────────────

    private void rollbackReservedItems(
            List<ReservedItem> reservedItems,
            OutboundOrder order,
            Long userId) {
        for (ReservedItem item : reservedItems) {
            try {
                if (item.isHot()) {
                    inventoryRedisService.increaseStock(
                            item.warehouseId(), item.zoneId(),
                            item.productId(), item.quantity(), order.getOrderCode());
                    log.info("[ROLLBACK] Hot item hoàn Redis: productId={} qty={}",
                            item.productId(), item.quantity());
                } else {
                    InventoryRequest req = new InventoryRequest(
                            item.warehouseId(), item.zoneId(),
                            item.productId(), item.quantity(),
                            userId, order.getOrderCode());
                    inventoryService.addStock(req);
                    log.info("[ROLLBACK] Cold item hoàn DB: productId={} qty={}",
                            item.productId(), item.quantity());
                }
            } catch (Exception e) {
                // Không throw — tiếp tục rollback item tiếp theo
                // Log ERROR để alert xử lý thủ công nếu cần
                log.error("[ROLLBACK FAILED] productId={} orderCode={} — CẦN XỬ LÝ THỦ CÔNG",
                        item.productId(), order.getOrderCode(), e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────


    //Check quyền permission warehouse
    private void checkPermssion(OutboundOrder order, Long userId) {
        //Check trung warehouseIds
        Set<Long> warehouseIds = order.getOutboundOrderItems().stream()
                .map(item -> item.getWarehouse().getId())
                .collect(Collectors.toSet());

        //kiem tra xem permission có đúng không
        permissionWarehouseService.checkPermissionByListWarehouseId(userId, warehouseIds,
                PermissionCode.OUTBOUND_COMPLETE);
    }

    /**
     * TODO: thêm field vào Product entity để tránh check tên sản phẩm dễ sai:
     *
     * @Column(name = "is_hot_item") private boolean hotItem;
     * rồi đổi thành: return product.isHotItem();
     */
    private boolean hotItem(Product product) {
        String name = product.getName().toLowerCase();
        return name.contains("laptop") || name.contains("raw");
    }

    private OutboundResponse mapToResponse(OutboundOrder order) {
        List<OutboundItemRequest> itemDTOs = order.getOutboundOrderItems().stream()
                .map(item -> new OutboundItemRequest(
                        item.getProduct().getId(),
                        item.getWarehouse().getId(),
                        item.getZone().getId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        return new OutboundResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomer().getName(),
                order.getStatus().toString(),
                order.getOrderDateTime(),
                order.getNote(),
                itemDTOs);
    }
}