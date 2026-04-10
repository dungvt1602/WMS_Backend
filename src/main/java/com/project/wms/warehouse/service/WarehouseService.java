package com.project.wms.warehouse.service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy kho theo id
    public WarehouseResponse getWarehouseById(Long id) {
        WarehouseEntity warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với id: " + id));
        return toResponse(warehouse);
    }

    // tắt hoạt động của kho
    @Transactional
    public void disableWarehouse(String code) {
        WarehouseEntity warehouse = warehouseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với mã: " + code));
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    // xóa warehouse đó khỏi database
    @Transactional
    public void deleteWarehouse(String code) {
        WarehouseEntity warehouse = warehouseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với mã: " + code));
        warehouseRepository.delete(warehouse);
    }

    // cập nhập kho theo id hoặc theo request
    @Transactional
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
