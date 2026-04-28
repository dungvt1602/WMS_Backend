package com.project.wms.inventory.service;

import com.project.wms.inventory.dto.InventoryDashboardResponse;
import com.project.wms.inventory.dto.InventorySummaryResponse;
import com.project.wms.inventory.dto.StockMovementResponse;
import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.entity.StockMovement;
import com.project.wms.inventory.entity.TransferReport;
import com.project.wms.inventory.repository.InventoryRepository;
import com.project.wms.inventory.repository.MovementRepository;
import com.project.wms.inventory.repository.TransferReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReportService {

    private final InventoryRepository inventoryRepository;
    private final MovementRepository movementRepository;
    private final TransferReportRepository transferReportRepository;

    @Transactional(readOnly = true)
    public Page<InventorySummaryResponse> getInventorySummary(Long warehouseId, Long productId, Pageable pageable) {
        if (warehouseId != null && productId != null) {
            return inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId, pageable)
                    .map(this::toInventorySummaryResponse);
        }
        if (warehouseId != null) {
            return inventoryRepository.findByWarehouseId(warehouseId, pageable)
                    .map(this::toInventorySummaryResponse);
        }
        if (productId != null) {
            return inventoryRepository.findByProductId(productId, pageable)
                    .map(this::toInventorySummaryResponse);
        }
        return inventoryRepository.findAll(pageable)
                .map(this::toInventorySummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getStockHistory(Long productId, Long warehouseId, Pageable pageable) {
        Page<StockMovement> movements;
        if (productId != null && warehouseId != null) {
            movements = movementRepository.findByProductIdAndWarehouseId(productId, warehouseId, pageable);
        } else if (productId != null) {
            movements = movementRepository.findByProductId(productId, pageable);
        } else if (warehouseId != null) {
            movements = movementRepository.findByWarehouseId(warehouseId, pageable);
        } else {
            movements = movementRepository.findAll(pageable);
        }

        return movements.map(m -> new StockMovementResponse(
                m.getId(),
                m.getProductId(),
                m.getProductName(),
                m.getWarehouseId(),
                m.getWarehouseName(),
                m.getMovementType(),
                m.getQuantity(),
                m.getReferenceCode(),
                m.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Page<TransferReport> getTransferHistory(Long warehouseId, Pageable pageable) {
        if (warehouseId == null) {
            return transferReportRepository.findAll(pageable);
        }
        return transferReportRepository.findByFromWarehouseIdOrToWarehouseId(warehouseId, warehouseId, pageable);
    }

    @Transactional(readOnly = true)
    public InventoryDashboardResponse getDashboard(Long warehouseId, Integer lowStockThreshold) {
        int threshold = (lowStockThreshold == null || lowStockThreshold < 0) ? 10 : lowStockThreshold;

        long totalProducts = inventoryRepository.countDistinctProducts(warehouseId);
        long totalWarehouses = inventoryRepository.countDistinctWarehouses(warehouseId);
        long totalQuantity = inventoryRepository.sumQuantity(warehouseId);
        long totalAvailableQuantity = inventoryRepository.sumAvailableQuantity(warehouseId);
        long lowStockItems = inventoryRepository.countLowStockItems(warehouseId, threshold);

        List<StockMovement> recentMovements = warehouseId == null
                ? movementRepository.findTop10ByOrderByCreatedAtDesc()
                : movementRepository.findTop10ByWarehouseIdOrderByCreatedAtDesc(warehouseId);

        List<StockMovementResponse> recentMovementResponses = recentMovements.stream()
                .map(m -> new StockMovementResponse(
                        m.getId(),
                        m.getProductId(),
                        m.getProductName(),
                        m.getWarehouseId(),
                        m.getWarehouseName(),
                        m.getMovementType(),
                        m.getQuantity(),
                        m.getReferenceCode(),
                        m.getCreatedAt()))
                .toList();

        return new InventoryDashboardResponse(
                totalProducts,
                totalWarehouses,
                totalQuantity,
                totalAvailableQuantity,
                lowStockItems,
                recentMovementResponses);
    }

    private InventorySummaryResponse toInventorySummaryResponse(Inventory inventory) {
        Long zoneId = inventory.getZone() != null ? inventory.getZone().getId() : null;
        String zoneName = inventory.getZone() != null ? inventory.getZone().getName() : null;

        return new InventorySummaryResponse(
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                zoneId,
                zoneName,
                inventory.getQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getStatus());
    }
}
