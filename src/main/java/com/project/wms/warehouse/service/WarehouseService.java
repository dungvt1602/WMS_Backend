package com.project.wms.warehouse.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.warehouse.dto.WarehouseRequest;
import com.project.wms.warehouse.dto.WarehouseResponse;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.repository.WarehouseRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    // Tạo kho mới
    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        // Kiểm tra mã kho đã tồn tại chưa
        if (warehouseRepository.existsByCode(request.code())) {
            throw new RuntimeException("Mã kho " + request.code() + " đã tồn tại");
        }
        // Tạo kho mới
        WarehouseEntity warehouse = WarehouseEntity.builder()
                .code(request.code())
                .name(request.name())
                .location(request.location())
                .capacity(request.capacity())
                .active(request.active())
                .build();
        // Lưu kho
        WarehouseEntity savedWarehouse = warehouseRepository.save(warehouse);
        // Trả về response
        return toResponse(savedWarehouse);
    }

    // Lấy danh sách tất cả các kho
    @CacheEvict(value = "warehouses", allEntries = true)
    public Page<WarehouseResponse> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // Lấy kho theo id
    @Cacheable(key = "#id", value = "warehouses")
    public WarehouseResponse getWarehouseById(Long id) {
        WarehouseEntity warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với id: " + id));
        return toResponse(warehouse);
    }

    // tắt hoạt động của kho
    @Transactional
    public void disableWarehouse(String code) {
        throw new RuntimeException("【Chính sách Enterprise】: "
                + "Cấm xóa cứng (Hard Delete) để bảo toàn lịch sử Nhập/Xuất kho. Vui lòng gọi API Disable Kho!");
    }

    // xóa warehouse đó khỏi database
    @Transactional
    @CacheEvict(key = "#id", value = "warehouses")
    public void deleteWarehouse(String code) {
        WarehouseEntity warehouse = warehouseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với mã: " + code));
        warehouseRepository.delete(warehouse);
    }

    // cập nhập kho theo id hoặc theo request
    @Transactional
    @CachePut(key = "#id", value = "warehouses")
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        WarehouseEntity warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với id: " + id));

        // cập nhập warehouse theo request
        warehouse.setCode(request.code());
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
        warehouse.setCapacity(request.capacity());
        warehouse.setActive(request.active());

        WarehouseEntity updatedWarehouse = warehouseRepository.save(warehouse);
        return toResponse(updatedWarehouse);

    }

    private WarehouseResponse toResponse(WarehouseEntity warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCapacity(),
                warehouse.isActive());
    }

}
