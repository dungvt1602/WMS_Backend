package com.project.wms.inventory.service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.inventory.entity.Inventory;
import com.project.wms.inventory.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryWarmupService implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    // Lưu ý: Dùng StringRedisTemplate thay cho RedisTemplate cũ
    // vì StringRedisTemplate lưu dữ liệu ở dạng chữ nguyên thủy, có thể thao tác
    // Trừ (DECR) được!
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(readOnly = true) // 1. Tối ưu Hibernate: tắt dirty check, ổn định session
    public void run(String... args) throws Exception {
        log.info("🔥 Bắt đầu chiến dịch nạp vào Redis để cache hàng hot .....");

        // 2. Chỉ lấy những mặt hàng "Hot" (Laptop, Raw) ngay từ DB để tiết kiệm RAM
        // Lưu ý: Cần bổ sung method findHotItems trong Repository (xem mục 2 bên dưới)
        List<Inventory> hotItems = inventoryRepository.findHotItems("%Laptop%", "%Raw%"); // % là tất cả kí tự gì phía
                                                                                          // trước vd: "Kho laptop A"
                                                                                          // hoặc "Raw Material"

        if (hotItems.isEmpty()) {
            log.warn("Không có mặt hàng hot nào để load hoặc từ khóa sai rồi");
        }

        stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (Inventory inv : hotItems) {
                String key = "stock:" + inv.getWarehouse().getId() + ":" + inv.getZone().getId() + ":"
                        + inv.getProduct().getId();
                String value = String.valueOf(inv.getQuantity());

                connection.set(key.getBytes(), value.getBytes());
            }
            return null;
        });

        log.info("🎉 Hoàn tất nạp {} mặt hàng hot vào Redis", hotItems.size());
    }
}
