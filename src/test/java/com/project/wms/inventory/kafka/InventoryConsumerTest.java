package com.project.wms.inventory.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.service.InventoryService;

@ExtendWith(MockitoExtension.class)
class InventoryConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryConsumer inventoryConsumer;

    // ============ VIẾT 3 TEST CASES TỪ ĐÂY TRỞ XUỐNG ============

    // Case 1: Nhận event OUTBOUND → phải gọi removeStock() đúng 1 lần
    @Test
    @DisplayName("1. Nhận sự kiện OUTBOUND → gọi removeStock thành công")
    void consume_OutboundEvent_ShouldCallRemoveStock() {
        // TODO: Tạo InventoryEvent với actionType = "OUTBOUND"
        InventoryEvent inventoryEvent = new InventoryEvent(1L, 1L, 10, "OUTBOUND", "ORDER-001");
        // TODO: Gọi inventoryConsumer.consume(event)
        inventoryConsumer.consume(inventoryEvent);
        // TODO: verify inventoryService.removeStock() được gọi 1 lần
        verify(inventoryService, times(1)).removeStock(any(InventoryRequest.class));
        // TODO: verify inventoryService.addStock() KHÔNG được gọi
        verify(inventoryService, never()).addStock(any(InventoryRequest.class));
    }

    // Case 2: Nhận event INBOUND → phải gọi addStock() đúng 1 lần
    @Test
    @DisplayName("2. Nhận sự kiện INBOUND → gọi addStock thành công")
    void consume_InboundEvent_ShouldCallAddStock() {
        // TODO: Tạo InventoryEvent với actionType = "INBOUND"
        InventoryEvent inventoryEvent = new InventoryEvent(1L, 1L, 10, "INBOUND", "ORDER-001");
        // TODO: Gọi inventoryConsumer.consume(event)
        inventoryConsumer.consume(inventoryEvent);
        // TODO: verify inventoryService.addStock() được gọi 1 lần
        verify(inventoryService, times(1)).addStock(any(InventoryRequest.class));
        // TODO: verify inventoryService.removeStock() KHÔNG được gọi
        verify(inventoryService, never()).removeStock(any(InventoryRequest.class));
    }

    // Case 3: removeStock() bị lỗi → Consumer phải ném lại exception (để Kafka
    // retry)
    @Test
    @DisplayName("3. removeStock lỗi → Consumer ném lại exception cho Kafka retry")
    void consume_FailedEvent_ShouldThrowException() {
        // TODO: Tạo InventoryEvent với actionType = "OUTBOUND"
        InventoryEvent inventoryEvent = new InventoryEvent(1L, 1L, 10, "OUTBOUND", "ORDER-001");
        // TODO: Dùng when(...).thenThrow() để giả lập removeStock() bị lỗi
        when(inventoryService.removeStock(any(InventoryRequest.class)))
                .thenThrow(new RuntimeException("Lỗi kết nối DB"));
        // TODO: assertThrows(RuntimeException.class, () ->
        // inventoryConsumer.consume(event))
        assertThrows(RuntimeException.class, () -> inventoryConsumer.consume(inventoryEvent));
    }

}
