package com.project.wms.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.dto.InventoryResponse;
import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.repository.InventoryRepository;
import com.project.wms.inventory.repository.MovementRepository;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private InventoryService inventoryService;

    // ============ VIẾT TEST CASES TỪ ĐÂY TRỞ XUỐNG ============

    @Test
    @DisplayName("1. Nhập kho cho sản phẩm mới - Phải tạo bản ghi mới")
    void addStock_NewProduct_ShouldCreateInventory() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 50, "REF-001");

        Product mockProduct = new Product();
        mockProduct.setId(101L);
        mockProduct.setName("Laptop A");

        WarehouseEntity mockWarehouse = new WarehouseEntity();
        mockWarehouse.setId(1L);
        mockWarehouse.setName("Kho Trung Tâm");

        when(productRepository.findById(101L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mockWarehouse));

        Inventory savedInventory = new Inventory();
        savedInventory.setId(999L);
        savedInventory.setProduct(mockProduct);
        savedInventory.setWarehouse(mockWarehouse);
        savedInventory.setQuantity(50);
        
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);
        when(inventoryRepository.findByWarehouseIdAndProductId(1L, 101L)).thenReturn(Optional.empty());

        InventoryResponse response = inventoryService.addStock(request);

        assertNotNull(response);
        assertEquals(999L, response.id());
        assertEquals(50, response.quantity());
        assertEquals("Laptop A", response.productName());

        verify(productRepository, times(1)).findById(101L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("2. Cập nhật số lượng tồn kho - Phải update bản ghi cũ")
    void addStock_ExistingProduct_ShouldIncreaseQuantity() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 5, "REF-001");

        Product mockProduct = new Product();
        mockProduct.setId(101L);
        mockProduct.setName("Laptop A");

        WarehouseEntity mockWarehouse = new WarehouseEntity();
        mockWarehouse.setId(1L);
        mockWarehouse.setName("Kho Trung Tâm");

        when(productRepository.findById(101L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mockWarehouse));

        Inventory savedInventory = new Inventory();
        savedInventory.setId(999L);
        savedInventory.setProduct(mockProduct);
        savedInventory.setWarehouse(mockWarehouse);
        savedInventory.setQuantity(10);
        
        when(inventoryRepository.findByWarehouseIdAndProductId(1L, 101L)).thenReturn(Optional.of(savedInventory));

        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.addStock(request);

        assertNotNull(response);
        assertEquals(15, response.quantity(), "Số lượng sau khi cộng dồn phải là 15");
        assertEquals(999L, response.id());
        assertEquals("Laptop A", response.productName());

        verify(productRepository, times(1)).findById(101L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("3. Nhập kho cho sản phẩm không tồn tại - Phải ném ngoại lệ")
    void addStock_ProductNotFound_ShouldThrowException() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 5, "REF-001");

        when(productRepository.findById(101L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.addStock(request));

        verify(productRepository, times(1)).findById(101L);
        verify(warehouseRepository, never()).findById(anyLong());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("1. Test truong hop xuat kho ma co du quantity phu hop")
    void removeStock_EnoughQuantity_ShouldDecrease() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 5, "REF-001");

        Product mockProduct = new Product();
        mockProduct.setId(101L);
        mockProduct.setName("Laptop A");

        WarehouseEntity mockWarehouse = new WarehouseEntity();
        mockWarehouse.setId(1L);
        mockWarehouse.setName("Kho Trung Tâm");

        Inventory savedInventory = new Inventory();
        savedInventory.setId(999L);
        savedInventory.setProduct(mockProduct);
        savedInventory.setWarehouse(mockWarehouse);
        savedInventory.setQuantity(10);
        savedInventory.setAvailableQuantity(10);

        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 101L)).thenReturn(Optional.of(savedInventory));

        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.removeStock(request);

        assertNotNull(response);
        assertEquals(5, response.quantity(), "So luong sau khi tru di phai la 5");
        assertEquals(999L, response.id());
        assertEquals("Laptop A", response.productName());

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("2. Test truong hop xuat kho ma khong du quantity")
    void removeStock_NotEnoughQuantity_ShouldThrowException() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 15, "REF-001");

        Product mockProduct = new Product();
        mockProduct.setId(101L);
        mockProduct.setName("Laptop A");

        WarehouseEntity mockWarehouse = new WarehouseEntity();
        mockWarehouse.setId(1L);
        mockWarehouse.setName("Kho Trung Tâm");

        Inventory savedInventory = new Inventory();
        savedInventory.setId(999L);
        savedInventory.setProduct(mockProduct);
        savedInventory.setWarehouse(mockWarehouse);
        savedInventory.setQuantity(10);
        savedInventory.setAvailableQuantity(10);
        
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 101L)).thenReturn(Optional.of(savedInventory));

        assertThrows(RuntimeException.class, () -> inventoryService.removeStock(request));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("3. Test truong hop xuat kho dung voi so luong quantity bang so luong ton kho")
    void removeStock_ExactQuantity_ShouldSetToZero() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 10, "REF-001");

        Product mockProduct = new Product();
        mockProduct.setId(101L);
        mockProduct.setName("Laptop A");

        WarehouseEntity mockWarehouse = new WarehouseEntity();
        mockWarehouse.setId(1L);
        mockWarehouse.setName("Kho Trung Tâm");

        Inventory savedInventory = new Inventory();
        savedInventory.setId(999L);
        savedInventory.setProduct(mockProduct);
        savedInventory.setWarehouse(mockWarehouse);
        savedInventory.setQuantity(10);
        savedInventory.setAvailableQuantity(10);
        
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 101L)).thenReturn(Optional.of(savedInventory));

        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.removeStock(request);

        assertNotNull(response);
        assertEquals(0, response.quantity());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedEntity = inventoryCaptor.getValue();
        assertEquals(0, savedEntity.getQuantity(), "Thực thể lưu vào DB phải có số lượng là 0");
    }

    @Test
    @DisplayName("4. Test truong hop san pham khong co trong kho")
    void removeStock_ProductNotInInventory_ShouldThrowException() {
        InventoryRequest request = new InventoryRequest(1L, 101L, 10, "REF-001");

        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 101L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.removeStock(request));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}
