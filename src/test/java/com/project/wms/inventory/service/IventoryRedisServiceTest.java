package com.project.wms.inventory.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.project.wms.inventory.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
public class IventoryRedisServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryRedisService inventoryRedisService;

    @Mock
    private DefaultRedisScript<Long> decreaseStockScript;

    @Test
    @DisplayName(" 1. Trừ xuat kho số lượng phù hợp chuẩn")
    void decreaseStock_Enough_ShouldReturnTrue() {
        // 1. Chuẩn bị dữ liệu mẫu trong DB
        // (Giả sử bạn đã có sẵn dữ liệu trong DB)
        Long warehouseId = 1L;
        Long productId = 1L;
        int quantity = 3;
        Long zoneId = 1L;
        Long remaining = 7L;
        String key = "stock:" + warehouseId + ":" + zoneId + ":" + productId;
        // 2. Gọi hàm cần test
        when(stringRedisTemplate.execute(
                eq(decreaseStockScript),
                eq(Collections.singletonList(key)),
                eq(String.valueOf(quantity)))).thenReturn(remaining);

        // 3 trả kết quả
        boolean result = inventoryRedisService.decreaseStockAtomic(warehouseId, zoneId, productId, quantity);

        // 4. Verify (Xác nhận hành vi thực hiện)
        assertTrue(result, "Nên trả về true khi đủ hàng");

    }

    // 2. Test case: Không đủ hàng (số lượng âm)
    @Test
    @DisplayName("2. Trừ xuất kho không đủ hàng (số lượng âm)")
    void decreaseStock_NotEnough_ShouldReturnFalse() {
        // 1. Chuẩn bị dữ liệu mẫu
        Long warehouseId = 1L;
        Long productId = 1L;
        Long zoneId = 1L;
        int quantity = 15; // Số lượng lớn hơn tồn kho
        String key = "stock:" + warehouseId + ":" + zoneId + ":" + productId;
        Long remaining = -5L;
        // 2. Gọi hàm cần test
        when(stringRedisTemplate.execute(
                eq(decreaseStockScript),
                eq(Collections.singletonList(key)),
                eq(String.valueOf(quantity)))).thenReturn(remaining);

        // 3 trả kết quả
        boolean result = inventoryRedisService.decreaseStockAtomic(warehouseId, zoneId, productId, quantity);

        // 4. Verify (Xác nhận hành vi thực hiện)
        assertFalse(result, "Nên trả về false khi không đủ hàng");

    }

}
