package com.project.wms.warehouse.service;

import com.project.wms.warehouse.entity.WarehouseZone;
import com.project.wms.warehouse.repository.WarehouseZoneRepository;
import com.project.wms.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseZoneService {

    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;

    public List<WarehouseZone> getZonesByWarehouseId(Long warehouseId) {
        return zoneRepository.findByWarehouseId(warehouseId);
    }

    public WarehouseZone createZone(WarehouseZone zone) {
        if (zoneRepository.findByCode(zone.getCode()).isPresent()) {
            throw new RuntimeException("Mã Khu/Kệ đã tồn tại");
        }
        if (!warehouseRepository.existsById(zone.getWarehouse().getId())) {
            throw new RuntimeException("Kho không tồn tại");
        }
        return zoneRepository.save(zone);
    }
}
