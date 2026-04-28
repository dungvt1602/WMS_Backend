package com.project.wms.inventory.service;

import com.project.wms.auth.dto.PermissionCode;
import com.project.wms.auth.service.PermissionWarehouseService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.auth.utils.SecurityUtils;
import com.project.wms.common.exception.BussinessException;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.dto.InventoryResponse;
import com.project.wms.inventory.dto.InventoryTransferRequest;
import com.project.wms.inventory.dto.InventoryTransferResponse;
import com.project.wms.inventory.dto.StockMovementEvent;
import com.project.wms.inventory.dto.TransferStockEvent;
import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.entity.InventoryLog;
import com.project.wms.inventory.entity.StockMovement;
import com.project.wms.inventory.entity.StockTransferMovement;
import com.project.wms.inventory.enums.MovementType;
import com.project.wms.inventory.repository.InventoryLogRepository;
import com.project.wms.inventory.repository.InventoryRepository;
import com.project.wms.inventory.repository.MovementRepository;
import com.project.wms.inventory.repository.TransferMovementRepository;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.entity.WarehouseZone;
import com.project.wms.warehouse.repository.WarehouseRepository;
import com.project.wms.warehouse.repository.WarehouseZoneRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final ProductRepository productRepository;
    private final TransferMovementRepository transferMovementRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final InventoryLogRepository inventoryLogRepository;
    private final PermissionWarehouseService permissionWarehouseService;

    @Transactional
    public InventoryResponse addStock(InventoryRequest request) {
        // 1. Lôi cổ Product và Warehouse từ database lên để check xem nó có tồn tại
        // không
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        WarehouseEntity warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new RuntimeException("Kho không tồn tại!"));

        WarehouseZone zone = null;
        if (request.zoneId() != null) {
            zone = warehouseZoneRepository.findById(request.zoneId())
                    .orElseThrow(() -> new RuntimeException("Khu vực (Zone) không tồn tại!"));
        }

        // 2. Tìm xem kho và khu này đã từng chứa đồ này chưa?
        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndProductIdAndZoneId(request.warehouseId(), request.productId(),
                        request.zoneId())
                .orElse(null);

        if (inventory == null) {
            // Trường hợp 1: Món này lần đầu chui vào kho -> Tạo mới thẻ tồn kho
            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .zone(zone)
                    .quantity(request.quantity())
                    .availableQuantity(request.quantity())
                    .location(zone != null ? zone.getCode() : "N/A")
                    .status("ACTIVE")
                    .build();
        }

        // Tim id nguoi thuc hien

        // luu bien động số dư - log ra
        eventPublisher.publishEvent(new StockMovementEvent(
                product.getId(),
                product.getName(),
                zone.getId(),
                zone.getName(),
                warehouse.getId(),
                warehouse.getName(),
                request.userId(),
                "",
                request.referenceCode(),
                MovementType.INBOUND,
                request.quantity()));

        // 3. Save xuống DB
        int row = inventoryRepository.addStock(inventory.getId(), request.quantity());
        if (row == 0) {
            throw new RuntimeException(
                    "Thêm hàng không thành công vi quantity < 0  hoac co nguoi khac dang thao tac");
        }
        Inventory newInventory = inventoryRepository.findById(inventory.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tồn kho"));

        return toResponse(newInventory);
    }

    @Transactional
    public InventoryResponse removeStock(InventoryRequest request, String orderCode) {
        // Tìm thẻ kho tại chính xác Vị trí đó
        Inventory inventory = inventoryRepository
                .findWithLockByWarehouseIdAndProductIdAndZoneId(request.warehouseId(),
                        request.productId(), request.zoneId())
                .orElseThrow(() -> new RuntimeException(
                        "Sản phẩm không có trong kho (hoặc vị trí) này!"));

        // Kiểm tra xem số lượng trong kho có đủ để xuất không
        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new RuntimeException(
                    "Không đủ hàng trong kho! Chỉ còn " + inventory.getAvailableQuantity()
                            + " sản phẩm.");
        }

        // ghi bien dong so du

        // luu bien động số dư - log ra
        eventPublisher.publishEvent(new StockMovementEvent(
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getZone().getId(),
                inventory.getZone().getName(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                request.userId(),
                "",
                request.referenceCode(),
                MovementType.OUTBOUND,
                request.quantity()));

        // 3. Save xuống DB
        int rows = inventoryRepository.remakeStock(inventory.getId(), request.quantity());

        if (rows == 0) {
            throw new RuntimeException("Không đủ hàng trong kho (hoặc vị trí co nguoi dang sua) này!");
        }
        Inventory newInventory = inventoryRepository.findById(inventory.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tồn kho"));

        // Luu log

        inventoryLogRepository.save(InventoryLog.builder()
                .orderCode(orderCode + request.productId())
                .productId(request.productId())
                .quantity(request.quantity())
                .movementType("OUTBOUND")
                .build());
        return toResponse(newInventory);
    }

    @Transactional
    public InventoryTransferResponse transferStock(InventoryTransferRequest request) {

        //Lay du lieu nguoi dung
        Long userId = SecurityUtils.getCurrentUserId();
        //kiem tra quyen han cua nguoi dung
        checkPermissionTransfer(request, userId);
        // Lấy key dữ liệu
        String fromKey = request.fromWarehouseId() + ":" + request.fromZoneId() + ":" + request.productId();
        String toKey = request.toWarehouseId() + ":" + request.toZoneId() + ":" + request.productId();

        Inventory source;
        Inventory target;

        // Kiểm tra thứ tự của 2 khóa để khóa theo thứ tự ID/Key cố định (Chống
        // Deadlock)
        if (fromKey.compareTo(toKey) < 0) {
            source = getOrCreateInventoryWithLock(request.fromWarehouseId(), request.productId(),
                    request.fromZoneId());
            target = getOrCreateInventoryWithLock(request.toWarehouseId(), request.productId(),
                    request.toZoneId());
        } else {
            // Khóa Đích trước, Nguồn sau nhưng biến gán vẫn phải chuẩn
            target = getOrCreateInventoryWithLock(request.toWarehouseId(), request.productId(),
                    request.toZoneId());
            source = getOrCreateInventoryWithLock(request.fromWarehouseId(), request.productId(),
                    request.fromZoneId());
        }

        // Kiểm tra số lượng khả dụng (Available Quantity)
        if (source.getAvailableQuantity() < request.quantity()) {
            throw new BussinessException("Không đủ số lượng hàng khả dụng để chuyển!");
        }

        // Cập nhật số lượng cho cả 2 (Quantity & AvailableQuantity)
        source.setQuantity(source.getQuantity() - request.quantity());
        source.setAvailableQuantity(source.getAvailableQuantity() - request.quantity());

        target.setQuantity(target.getQuantity() + request.quantity());
        target.setAvailableQuantity(target.getAvailableQuantity() + request.quantity());
        

        // luu report bien dong so luong kho nguon
        eventPublisher.publishEvent(new TransferStockEvent(
                source.getProduct().getId(),
                source.getProduct().getName(),
                source.getWarehouse().getId(),
                source.getWarehouse().getName(),
                source.getZone().getId(),
                source.getZone().getName(),
                target.getWarehouse().getId(),
                target.getWarehouse().getName(),
                target.getZone().getId(),
                target.getZone().getName(),
                request.quantity(),
                request.referenceCode(),
                userId,
                MovementType.TRANSFER));
        // Lưu biến động
        StockTransferMovement movement = saveTransferMovement(source, target, request.quantity(),
                request.referenceCode());

        return InventoryTransferResponse.success("Chuyển kho thành công " + request.quantity() + " từ "
                + source.getWarehouse().getName() + " đến " + target.getWarehouse().getName());

    }

    // Hàm xử lí chuyển kho (nội bộ và ngoài nội bộ)
    /*
     *
     *
     *
     * -----------Helper Function--------------------
     *
     *
     *
     * */

    //Hàm check quyền chuyển ko
    private void checkPermissionTransfer(InventoryTransferRequest request, Long userId) {
        Long fromId = request.fromWarehouseId();
        Long toId = request.toWarehouseId();

        permissionWarehouseService.checkPermssionForTransfer(
                userId,
                fromId, toId, PermissionCode.INVENTORY_ADJUST);

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
    private StockTransferMovement saveTransferMovement(Inventory fromStock, Inventory toStock, int quantity,
                                                       String referenceCode) {
        StockTransferMovement movement = StockTransferMovement.builder()
                .product(fromStock.getProduct())
                .fromWarehouse(fromStock.getWarehouse())
                .toWarehouse(toStock.getWarehouse())
                .fromZone(fromStock.getZone())
                .toZone(toStock.getZone())
                .quantity(quantity)
                .referenceCode(referenceCode)
                .build();
        return transferMovementRepository.save(movement);
    }

    private Inventory getOrCreateInventoryWithLock(Long warehouseId, Long productId, Long zoneId) {
        // 1. Cố gắng tìm và khóa nếu đã tồn tại
        return inventoryRepository
                .findWithLockByWarehouseIdAndProductIdAndZoneId(warehouseId, productId, zoneId)
                .orElseGet(() -> {
                    try {
                        // 2. Tạo mới nếu chưa có
                        Inventory newInv = Inventory.builder()
                                .warehouse(warehouseRepository
                                        .getReferenceById(warehouseId))
                                .product(productRepository.getReferenceById(productId))
                                .zone(zoneId != null ? warehouseZoneRepository
                                                       .getReferenceById(zoneId) : null)
                                .quantity(0)
                                .availableQuantity(0)
                                .status("ACTIVE")
                                .build();
                        return inventoryRepository.saveAndFlush(newInv);
                    } catch (DataIntegrityViolationException e) {
                        // 3. Nếu có thằng khác nhanh tay tạo trước rồi, thì tìm lại lần nữa
                        return inventoryRepository
                                .findWithLockByWarehouseIdAndProductIdAndZoneId(
                                        warehouseId, productId, zoneId)
                                .orElseThrow(() -> new RuntimeException(
                                        "Lỗi xung đột dữ liệu kho!"));
                    }
                });
    }
}
