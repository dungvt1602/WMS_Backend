package com.project.wms.inbound.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.auth.utils.SecurityUtils;
import com.project.wms.common.enums.OrderStatus;
import com.project.wms.customer.entity.Customer;
import com.project.wms.customer.repository.CustomerRepository;
import com.project.wms.inbound.dto.InboundItemRequest;
import com.project.wms.inbound.dto.InboundRequest;
import com.project.wms.inbound.dto.InboundResponse;
import com.project.wms.inbound.entity.InboundOrder;
import com.project.wms.inbound.entity.InboundOrderItems;

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
    private WarehouseRepository warehouseRepository;
    private ProductRepository productRepository;
    private InventoryService inventoryService;
    private CustomerRepository customerRepository;

    // Tạo hóa đơn nhập kho
    @Transactional
    public InboundResponse createOrder(InboundRequest request) {

        // tạo phiếu nhập kho
        Customer customer = customerRepository.findByName(request.supplierName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp: " + request.supplierName()));

        if (customer.getType() != com.project.wms.customer.entity.CustomerType.SUPPLIER) {
            throw new RuntimeException("Đối tác " + request.supplierName() + " không phải là Nhà cung cấp!");
        }

        InboundOrder order = new InboundOrder();
        order.setOrderNumber("INB-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDateTime(LocalDateTime.now());
        order.setNote(request.note());
        order.setCustomer(customer);

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

    public InboundResponse completeOrder(Long orderId, Long zoneId) {

        // 1. Lấy order trước
        InboundOrder order = inboundOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order không hợp lệ");
        }

        List<InboundOrderItems> items = order.getInboundOrderItems();

        Long userId = SecurityUtils.getCurrentUserId();

        // 2. add stock trước
        for (InboundOrderItems item : items) {
            InventoryRequest req = new InventoryRequest(
                    item.getWarehouse().getId(),
                    zoneId,
                    item.getProduct().getId(),
                    item.getQuantity(),
                    userId,
                    order.getOrderNumber());
            inventoryService.addStock(req);
        }

        // 3. update status cuối cùng (atomic)
        int updated = inboundOrderRepository.updateOrderStatus(
                orderId, OrderStatus.COMPLETED, OrderStatus.PENDING);

        if (updated == 0) {
            throw new RuntimeException("Order đã bị xử lý bởi request khác");
        }

        return mapToResponse(order);
    }

    @Transactional
    public InboundResponse cancelOrder(Long orderId) {
        int updateRows = inboundOrderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED, OrderStatus.PENDING);
        if (updateRows == 0) {
            throw new RuntimeException("Không tìm thấy đơn hàng hoặc đơn hàng không ở trạng thái PENDING");
        }
        InboundOrder order = inboundOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập kho để hủy"));

        return mapToResponse(order);
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
                order.getCustomer().getName(),
                order.getStatus().name(), // Chuyển Enum -> String để trả ra JSON
                order.getOrderDateTime(),
                order.getNote(),
                itemDTOs);
    }

}
