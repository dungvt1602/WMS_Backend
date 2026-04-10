package com.project.wms.outbound.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.common.enums.OrderStatus;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.repository.InventoryRepository;
import com.project.wms.inventory.service.InventoryService;
import com.project.wms.outbound.dto.OutboundItemRequest;
import com.project.wms.outbound.dto.OutboundRequest;
import com.project.wms.outbound.dto.OutboundResponse;
import com.project.wms.outbound.entity.OutboundOrder;
import com.project.wms.outbound.entity.OutboundOrderItems;
import com.project.wms.outbound.repository.OutboundItemRepository;
import com.project.wms.outbound.repository.OutboundRepository;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboundService {

        private final OutboundRepository outboundRepository;
        private final ProductRepository productRepository;
        private final WarehouseRepository warehouseRepository;
        private final InventoryService inventoryService;

        @Transactional
        public OutboundResponse createOrder(OutboundRequest request) {

                // tao phieu xuat kho
                OutboundOrder order = new OutboundOrder();
                order.setOrderCode("OUT-" + System.currentTimeMillis());
                order.setCustomerName(request.customerName());
                order.setOrderDateTime(LocalDateTime.now());
                order.setNote(request.note());

                // tao cac mat hang trong don xuat kho
                List<OutboundOrderItems> listItems = request.items().stream()
                                .map(item -> {
                                        OutboundOrderItems outboundItem = new OutboundOrderItems();
                                        outboundItem.setOutboundOrder(order);
                                        outboundItem.setCreatedAt(LocalDateTime.now());

                                        // tim san pham
                                        Product product = productRepository.findById(item.productId())
                                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                                        // tim nha kho
                                        WarehouseEntity warehouse = warehouseRepository.findById(item.warehouseId())
                                                        .orElseThrow(() -> new RuntimeException("Warehouse not found"));

                                        outboundItem.setProduct(product);
                                        outboundItem.setWarehouse(warehouse);
                                        outboundItem.setQuantity(item.quantity());

                                        return outboundItem;
                                }).collect(Collectors.toList());

                order.setStatus(OrderStatus.PENDING);
                order.setOutboundOrderItems(listItems);
                // luu phieu xuat bao database
                return mapToResponse(outboundRepository.save(order));

        }

        @Transactional
        public OutboundResponse completeOrder(Long orderId) {
                // Tim don hang co ton tai khong
                OutboundOrder order = outboundRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Khong tim thay phieu xuat kho" + orderId));

                // Kiem tra don hang da duoc xuat kho chua
                if (order.getStatus() != OrderStatus.PENDING) {
                        throw new RuntimeException(
                                        "Don hang da o trang thai " + order.getStatus() + ", khong the xuat kho");
                }

                // kiem tra don xuat co san pham nao khong
                if (order.getOutboundOrderItems() == null || order.getOutboundOrderItems().isEmpty()) {
                        throw new RuntimeException("Don hang khong co san pham nao");
                }

                // cap nhap hang ton kho
                for (OutboundOrderItems item : order.getOutboundOrderItems()) {
                        InventoryRequest inventoryRequest = new InventoryRequest(
                                        item.getWarehouse().getId(),
                                        item.getProduct().getId(),
                                        item.getQuantity(),
                                        order.getOrderCode());
                        inventoryService.removeStock(inventoryRequest);
                }

                order.setStatus(OrderStatus.COMPLETED);
                OutboundOrder newOrder = outboundRepository.save(order);

                return mapToResponse(newOrder);

        }

        private OutboundResponse mapToResponse(OutboundOrder order) {
                // Map lồng danh sách items để trả về thông tin đầy đủ cho Frontend
                List<OutboundItemRequest> itemDTOs = order.getOutboundOrderItems().stream()
                                .map(item -> new OutboundItemRequest(
                                                item.getProduct().getId(),
                                                item.getWarehouse().getId(),
                                                item.getQuantity()))
                                .collect(Collectors.toList());

                return new OutboundResponse(
                                order.getId(),
                                order.getOrderCode(),
                                order.getCustomerName(),
                                order.getStatus().toString(),
                                order.getOrderDateTime(),
                                order.getNote(),
                                itemDTOs);
        }

}
