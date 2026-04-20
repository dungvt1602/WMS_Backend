package com.project.wms.inventory.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryRedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> decreaseStockScript;

    // Hàm trừ số lượng hàng hot trên redis mức độ automic
    public boolean decreaseStockAtomic(Long warehouseId, Long zoneId, Long productId, int quantity) {
        String key = "stock:" + warehouseId + ":" + zoneId + ":" + productId;

        // Chạy script: truyền Key và Đối số (quantity)
        Long result = stringRedisTemplate.execute(
                decreaseStockScript, // sử dung lua để xử lí logic hàm không được âm
                java.util.Collections.singletonList(key),
                String.valueOf(quantity));

        // Nếu result = -1 nghĩa là không đủ hàng (do ta quy định trong file lua)
        return result != null && result >= 0;
    }

}
