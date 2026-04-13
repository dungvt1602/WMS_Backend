package com.project.wms.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.dto.InventoryResponse;
import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.entity.StockMovement;
import com.project.wms.inventory.enums.MovementType;
import com.project.wms.inventory.repository.InventoryRepository;
import com.project.wms.inventory.repository.MovementRepository;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.repository.WarehouseRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final MovementRepository movementRepository;

    @Transactional
    public InventoryResponse addStock(InventoryRequest request) {
        // 1. Lôi cổ Product và Warehouse từ database lên để check xem nó có tồn tại
        // không
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        WarehouseEntity warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new RuntimeException("Kho không tồn tại!"));

        // 2. Tìm xem kho này đã từng chứa đồ này chưa?
        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndProductId(request.warehouseId(), request.productId())
                .orElse(null);

        if (inventory == null) {
            // Trường hợp 1: Món này lần đầu chui vào kho -> Tạo mới thẻ tồn kho
            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantity(request.quantity())
                    .availableQuantity(request.quantity())
                    .location("A1")
                    .status("ACTIVE")
                    .build();
        } else {
            // Trường hợp 2: Đã có sẵn trong kho -> Cộng dồn số lượng
            inventory.setQuantity(inventory.getQuantity() + request.quantity());
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.quantity());
        }

        // luu bien động số dư - log ra
        saveMovement(inventory, MovementType.INBOUND, request.quantity(), request.referenceCode());

        // 3. Save xuống DB
        Inventory savedInventory = inventoryRepository.save(inventory);

        return toResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse removeStock(InventoryRequest request) {
        // Tìm thẻ kho
        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndProductId(request.warehouseId(), request.productId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong kho này!"));

        // Kiểm tra xem số lượng trong kho có đủ để xuất không
        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new RuntimeException(
                    "Không đủ hàng trong kho! Chỉ còn " + inventory.getAvailableQuantity() + " sản phẩm.");
        }

        // Trừ đi số lượng
        inventory.setQuantity(inventory.getQuantity() - request.quantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());

        // ghi bien dong so du

        // luu bien động số dư - log ra
        saveMovement(inventory, MovementType.OUTBOUND, request.quantity(), request.referenceCode());

        // 3. Save xuống DB
        Inventory savedInventory = inventoryRepository.save(inventory);
        return toResponse(savedInventory);
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getQuantity(),
                inventory.getAvailableQuantity());
    }

    // tao bien dong so du
    private void saveMovement(Inventory inventory,
            MovementType movementType, int quantity, String referenceCode) {
        StockMovement movement = StockMovement.builder()
                .product(inventory.getProduct())
                .warehouse(inventory.getWarehouse())
                .movementType(movementType)
                .quantity(quantity)
                .referenceCode(referenceCode)
                .build();
        movementRepository.save(movement);
    }
}
