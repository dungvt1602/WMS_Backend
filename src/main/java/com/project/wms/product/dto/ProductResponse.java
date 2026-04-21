package com.project.wms.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
                Long id,
                String name,
                String sku,
                String description,
                String categoryName,
                String unit,
                BigDecimal price,
                int stock, // Số lượng tồn kho (sẽ tính toán ở Service)
                boolean active,
                LocalDateTime createdAt) {
}