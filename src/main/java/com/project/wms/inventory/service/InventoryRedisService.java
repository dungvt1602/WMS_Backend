package com.project.wms.inventory.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryRedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> decreaseStockScript;
    private final DefaultRedisScript<Long> increaseStockScript;

    // Hàm trừ tồn kho trong redis atomic
    public boolean decreaseStockAtomic(Long warehouseId, Long zoneId, Long productId, int quantity, String orderCode) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        String zone = (zoneId != null) ? zoneId.toString() : "0";
        String key = String.format("stock:%d:%s:%d", warehouseId, zone, productId);
        String processedKey = String.format("stock:process:%s", orderCode);

        Long result = stringRedisTemplate.execute(
                decreaseStockScript,
                java.util.Arrays.asList(key, processedKey),
                String.valueOf(quantity));

        if (result == null) {
            throw new RuntimeException("Redis execution failed for key: " + key);
        }

        if (result < 0) {
            return false;
        }

        if (result == 1) {
            log.warn("Đơn hàng {} đã được xử lý rồi", orderCode);
            return false;
        }

        return true;
    }

    public boolean increaseStock(Long warehouseId, Long zoneId, Long productId, int quantity, String orderCode) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        String zone = (zoneId != null) ? zoneId.toString() : "0";
        String key = String.format("stock:%d:%s:%d", warehouseId, zone, productId);
        String processedKey = String.format("stock:process:%s", orderCode);

        Long result = stringRedisTemplate.execute(
                increaseStockScript,
                java.util.Arrays.asList(key, processedKey),
                String.valueOf(quantity));

        if (result == null) {
            throw new RuntimeException("Redis execution failed for key: " + key);
        }

        if (result < 0) {
            return false;
        }

        if (result == 1) {
            log.warn("Đơn hàng {} đã được xử lý rồi", orderCode);
            return false;
        }

        return true;
    }

}
