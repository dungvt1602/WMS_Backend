package com.project.wms.inventory.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@ExtendWith(MockitoExtension.class)
public class IventoryRedisServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private InventoryRedisService inventoryRedisService;

    @Mock
    private DefaultRedisScript<Long> decreaseStockScript;

    @Mock
    private DefaultRedisScript<Long> increaseStockScript;

    @Test
    @DisplayName(" 1. Trừ xuat kho số lượng phù hợp chuẩn")
    void decreaseStock_Enough_ShouldReturnTrue() {
        // 1. Chuẩn bị dữ liệu mẫu trong DB
        // (Giả sử bạn đã có sẵn dữ liệu trong DB)
        Long warehouseId = 1L;
        Long productId = 1L;
        int quantity = 3;
        Long zoneId = 1L;
        String orderCode = "ORDER-001";
        Long remaining = 7L;

        String key = "stock:" + warehouseId + ":" + zoneId + ":" + productId;
        String processedKey = "stock:process:" + orderCode;

        // 2. Gọi hàm cần test
        when(stringRedisTemplate.execute(
                eq(decreaseStockScript),
                eq(Arrays.asList(key, processedKey)),
                eq(String.valueOf(quantity)))).thenReturn(remaining);

        // 3 trả kết quả
        boolean result = inventoryRedisService.decreaseStockAtomic(warehouseId, zoneId, productId, quantity, orderCode);

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
        String orderCode = "ORDER-002";
        String key = "stock:" + warehouseId + ":" + zoneId + ":" + productId;
        String processedKey = "stock:process:" + orderCode;
        Long remaining = -5L;
        // 2. Gọi hàm cần test
        when(stringRedisTemplate.execute(
                eq(decreaseStockScript),
                eq(Arrays.asList(key, processedKey)),
                eq(String.valueOf(quantity)))).thenReturn(remaining);

        // 3 trả kết quả
        boolean result = inventoryRedisService.decreaseStockAtomic(warehouseId, zoneId, productId, quantity, orderCode);

        // 4. Verify (Xác nhận hành vi thực hiện)
        assertFalse(result, "Nên trả về false khi không đủ hàng");

    }

    // 3. Test case: Tăng tồn kho thành công
    @Test
    @DisplayName("3. Tăng tồn kho thành công")
    void increaseStock_ShouldReturnTrue() {
        // 1. Chuẩn bị dữ liệu mẫu
        Long warehouseId = 1L;
        Long productId = 1L;
        Long zoneId = 1L;
        int quantity = 5;
        String orderCode = "ORDER-003";
        String key = "stock:" + warehouseId + ":" + zoneId + ":" + productId;
        String processedKey = "stock:process:" + orderCode;
        Long resultValue = 20L;

        // 2. Gọi hàm cần test
        when(stringRedisTemplate.execute(
                eq(increaseStockScript),
                eq(Arrays.asList(key, processedKey)),
                eq(String.valueOf(quantity)))).thenReturn(resultValue);

        // 3 trả kết quả
        boolean result = inventoryRedisService.increaseStock(warehouseId, zoneId, productId, quantity, orderCode);

        // 4. Verify (Xác nhận hành vi thực hiện)
        assertTrue(result, "Nên trả về true khi tăng tồn kho thành công");
    }

}
