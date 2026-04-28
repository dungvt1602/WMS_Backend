package com.project.wms.inventory.service;

import com.project.wms.auth.service.PermissionWarehouseService;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.dto.InventoryResponse;
import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.entity.InventoryLog;
import com.project.wms.inventory.repository.InventoryLogRepository;
import com.project.wms.inventory.repository.InventoryRepository;
import com.project.wms.inventory.repository.TransferMovementRepository;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.entity.WarehouseZone;
import com.project.wms.warehouse.repository.WarehouseRepository;
import com.project.wms.warehouse.repository.WarehouseZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseZoneRepository warehouseZoneRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private TransferMovementRepository transferMovementRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private InventoryLogRepository inventoryLogRepository;
    @Mock
    private PermissionWarehouseService permissionWarehouseService;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("addStock: tao moi ton kho va cap nhat thanh cong")
    void addStock_NewInventory_ShouldSucceed() {
        InventoryRequest request = new InventoryRequest(1L, 10L, 101L, 5, 9L, "REF-001");

        Product product = new Product();
        product.setId(101L);
        product.setName("Laptop A");

        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(1L);
        warehouse.setName("WH-1");

        WarehouseZone zone = new WarehouseZone();
        zone.setId(10L);
        zone.setName("Z-1");
        zone.setCode("Z1");

        Inventory created = Inventory.builder()
                .warehouse(warehouse)
                .product(product)
                .zone(zone)
                .quantity(5)
                .availableQuantity(5)
                .status("ACTIVE")
                .location("Z1")
                .build();
        created.setId(1000L);

        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseZoneRepository.findById(10L)).thenReturn(Optional.of(zone));
        when(inventoryRepository.findByWarehouseIdAndProductIdAndZoneId(1L, 101L, 10L))
                .thenReturn(Optional.empty());
        when(inventoryRepository.addStock(1000L, 5)).thenReturn(1);
        when(inventoryRepository.findById(1000L)).thenReturn(Optional.of(created));

        InventoryResponse response = inventoryService.addStock(request);

        assertNotNull(response);
        assertEquals(1000L, response.id());
        assertEquals(5, response.quantity());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("addStock: product khong ton tai")
    void addStock_ProductNotFound_ShouldThrow() {
        InventoryRequest request = new InventoryRequest(1L, 10L, 101L, 5, 9L, "REF-001");
        when(productRepository.findById(101L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.addStock(request));
        verify(warehouseRepository, never()).findById(any());
    }

    @Test
    @DisplayName("removeStock: tru ton kho thanh cong")
    void removeStock_Enough_ShouldSucceed() {
        InventoryRequest request = new InventoryRequest(1L, 10L, 101L, 3, 9L, "OUT-001");

        Product product = new Product();
        product.setId(101L);
        product.setName("Laptop A");

        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(1L);
        warehouse.setName("WH-1");

        WarehouseZone zone = new WarehouseZone();
        zone.setId(10L);
        zone.setName("Z-1");

        Inventory current = new Inventory();
        current.setId(2000L);
        current.setProduct(product);
        current.setWarehouse(warehouse);
        current.setZone(zone);
        current.setQuantity(10);
        current.setAvailableQuantity(10);

        Inventory updated = new Inventory();
        updated.setId(2000L);
        updated.setProduct(product);
        updated.setWarehouse(warehouse);
        updated.setZone(zone);
        updated.setQuantity(7);
        updated.setAvailableQuantity(7);

        when(inventoryRepository.findWithLockByWarehouseIdAndProductIdAndZoneId(1L, 101L, 10L))
                .thenReturn(Optional.of(current));
        when(inventoryRepository.remakeStock(2000L, 3)).thenReturn(1);
        when(inventoryRepository.findById(2000L)).thenReturn(Optional.of(updated));
        when(inventoryLogRepository.save(any(InventoryLog.class))).thenReturn(new InventoryLog());

        InventoryResponse response = inventoryService.removeStock(request, "OUT-001");

        assertNotNull(response);
        assertEquals(2000L, response.id());
        assertEquals(7, response.quantity());
        verify(inventoryLogRepository, times(1)).save(any(InventoryLog.class));
    }

    @Test
    @DisplayName("removeStock: khong du so luong")
    void removeStock_NotEnough_ShouldThrow() {
        InventoryRequest request = new InventoryRequest(1L, 10L, 101L, 20, 9L, "OUT-001");

        Inventory current = new Inventory();
        current.setId(2000L);
        current.setAvailableQuantity(10);

        when(inventoryRepository.findWithLockByWarehouseIdAndProductIdAndZoneId(1L, 101L, 10L))
                .thenReturn(Optional.of(current));

        assertThrows(RuntimeException.class, () -> inventoryService.removeStock(request, "OUT-001"));
        verify(inventoryRepository, never()).remakeStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("removeStock: inventory khong ton tai")
    void removeStock_NotFound_ShouldThrow() {
        InventoryRequest request = new InventoryRequest(1L, 10L, 101L, 2, 9L, "OUT-001");
        when(inventoryRepository.findWithLockByWarehouseIdAndProductIdAndZoneId(1L, 101L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.removeStock(request, "OUT-001"));
    }
}
