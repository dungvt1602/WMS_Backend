package com.project.wms.inbound.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.common.enums.OrderStatus;
import com.project.wms.inbound.dto.InboundItemRequest;
import com.project.wms.inbound.dto.InboundRequest;
import com.project.wms.inbound.dto.InboundResponse;
import com.project.wms.inbound.entity.InboundOrder;
import com.project.wms.inbound.entity.InboundOrderItems;
import com.project.wms.inbound.repository.InboundItemsRepository;
import com.project.wms.inbound.repository.InboundRepository;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.service.InventoryService;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.repository.WarehouseRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InboundService {
    private InboundRepository inboundOrderRepository;
    private InboundItemsRepository inboundItemsRepository;
    private WarehouseRepository warehouseRepository;
    private ProductRepository productRepository;
    private InventoryService inventoryService;

    // Tạo hóa đơn nhập kho
    @Transactional
    public InboundResponse createOrder(InboundRequest request) {

        // tạo phiếu nhập kho
        InboundOrder order = new InboundOrder();
        order.setOrderNumber("INB-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDateTime(LocalDateTime.now());
        order.setNote(request.note());
        order.setSupplierName(request.supplierName());

        // tạo vòng chuyển dto thành entity của Inbound order Items
        List<InboundOrderItems> items = request.items().stream().map(item -> {
            InboundOrderItems inboundItem = new InboundOrderItems();
            inboundItem.setCreatedAt(LocalDateTime.now());
            inboundItem.setInboundOrder(order);
            // tìm sản phẩm
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong phiếu tạo"));
            // tìm nhà kho yêu cầu
            WarehouseEntity warehouseEntity = warehouseRepository.findById(item.warehouseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kho lưu trữ trong yêu cầu"));

            inboundItem.setProduct(product);
            inboundItem.setWarehouse(warehouseEntity);
            inboundItem.setQuantity(item.quantity());
            inboundItem.setInboundOrder(order);
            return inboundItem;

        }).collect(Collectors.toList());

        order.setInboundOrderItems(items);
        InboundOrder savedOrder = inboundOrderRepository.save(order); // tự cập nhập item vì có caseda.all

        return mapToResponse(savedOrder);

    }

    @Transactional
    public InboundResponse completeOrder(Long orderId) {
        // tìm order cần hoàn thành
        InboundOrder order = inboundOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập kho"));

        // Kiểm tra trạng thái của đơn hàng - so sánh bằng enum, không phải String
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(
                    "Đơn hàng đã ở trạng thái " + order.getStatus() + ", không thể hoàn thành tiếp!");
        }

        // kiểm tra đơn hàng có sản phẩm nào không
        List<InboundOrderItems> items = order.getInboundOrderItems();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Đơn hàng không có sản phẩm nào");
        }

        // cập nhập số lượng tồn kho
        for (InboundOrderItems item : items) {
            InventoryRequest inventoryRequest = new InventoryRequest(
                    item.getWarehouse().getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    order.getOrderNumber());
            inventoryService.addStock(inventoryRequest);
        }

        // 4. Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.COMPLETED);
        return mapToResponse(inboundOrderRepository.save(order));
    }

    // Hàm chuyển đổi từ entity thành dto
    private InboundResponse mapToResponse(InboundOrder order) {
        // Chuyển đổi danh sách Entity Items sang DTO Items
        List<InboundItemRequest> itemDTOs = order.getInboundOrderItems().stream()
                .map(item -> new InboundItemRequest(
                        item.getProduct().getId(),
                        item.getWarehouse().getId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        // Trả về Response tổng thể
        return new InboundResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSupplierName(),
                order.getStatus().name(), // Chuyển Enum -> String để trả ra JSON
                order.getOrderDateTime(),
                order.getNote(),
                itemDTOs);
    }

}
