package com.project.wms.outbound.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.project.wms.auth.dto.PermissionCode;
import com.project.wms.auth.service.PermissionWarehouseService;
import com.project.wms.inventory.kafka.InventoryProducer;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.auth.utils.SecurityUtils;
import com.project.wms.common.enums.OrderStatus;
import com.project.wms.common.exception.ProductException;
import com.project.wms.common.exception.WarehouseException;
import com.project.wms.common.exception.WarehouseZoneException;
import com.project.wms.customer.entity.Customer;
import com.project.wms.customer.entity.CustomerType;
import com.project.wms.customer.repository.CustomerRepository;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.kafka.InventoryEvent;
import com.project.wms.inventory.service.InventoryRedisService;
import com.project.wms.inventory.service.InventoryService;
import com.project.wms.outbound.dto.OutboundItemRequest;
import com.project.wms.outbound.dto.OutboundRequest;
import com.project.wms.outbound.dto.OutboundResponse;
import com.project.wms.outbound.entity.OutboundOrder;
import com.project.wms.outbound.entity.OutboundOrderItems;
import com.project.wms.outbound.repository.OutboundRepository;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.entity.WarehouseZone;
import com.project.wms.warehouse.repository.WarehouseRepository;
import com.project.wms.warehouse.repository.WarehouseZoneRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final CustomerRepository customerRepository;
    private final PermissionWarehouseService permissionWarehouseService;

    // ------------Các hàm tạo hóa đơn, xử lí hệ thống phân tán, idempotency, outbox
    // pattern------------

    /**
     * Tạo outbound order.
     * <p>
     * KHÔNG đánh @Transactional ở đây — toàn bộ phase đọc (idempotency check,
     * validate, batch load) chạy ngoài transaction để không chiếm connection pool.
     * Chỉ phần save mới mở transaction (xem saveOrderTransactional bên dưới).
     */
    public OutboundResponse createOrder(OutboundRequest request) {


        if (request.requestId() == null || request.requestId().isEmpty()) {
            throw new RuntimeException("Mã yêu cầu không được để trống");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        checkPermssion(request, userId);

        // Kiểm tra request đó đã tạo order chưa
        Optional<OutboundOrder> existingOrder = outboundRepository.findByRequestId(request.requestId());
        if (existingOrder.isPresent()) {
            log.info("[IDEMPOTENT] requestId={} đã tồn tại, vui lòng kiểm tra lại", request.requestId());
            return mapToResponse(existingOrder.get());
        }

        // ── 3. Validate customer (ngoài transaction) ──────────────────────────
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tồn tại khách hàng có ID: " + request.customerId()));

        // if (customer.getType() != CustomerType.CUSTOMER) {
        // throw new InvalidCustomerTypeException();
        // }

        // ── 4. Batch load products + warehouses (ngoài transaction, 2 query) ──

        Set<Long> productIds = request.items().stream()
                .map(OutboundItemRequest::productId)
                .collect(Collectors.toSet());

        Set<Long> zoneIds = request.items().stream()
                .map(OutboundItemRequest::zoneId)
                .collect(Collectors.toSet());

        Set<Long> warehouseIds = request.items().stream()
                .map(OutboundItemRequest::warehouseId)
                .collect(Collectors.toSet());

        // Query sản phẩm bằng 1 câu lệnh sql

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, WarehouseEntity> warehouseMap = warehouseRepository.findAllById(warehouseIds).stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, w -> w));

        Map<Long, WarehouseZone> zoneMap = warehouseZoneRepository.findAllById(zoneIds).stream()
                .collect(Collectors.toMap(WarehouseZone::getId, z -> z));

        // 5 Validate dữ liệu product và warehouse và zone

        validateItems(request.items(), productMap, warehouseMap, zoneMap);

        // 6 Builder outbound order không cần select Database
        OutboundOrder order = buildOutboundOrder(request, customer, productMap, warehouseMap, zoneMap);
        return saveNewOrder(order, request.requestId());

    }

    // validate dữ liệu xem truyền sai id sản phẩm , nhà kho , và zone không
    private void validateItems(List<OutboundItemRequest> items, Map<Long, Product> productMap,
                               Map<Long, WarehouseEntity> warehouseMap,
                               Map<Long, WarehouseZone> zoneMap) {

        // Kiểm tra xem list có tồn tại keys không
        for (OutboundItemRequest item : items) {
            if (!productMap.containsKey(item.productId())) {
                throw new ProductException(
                        "Sản phẩm không tồn tại để thêm vào hàng hóa " + item.productId());
            }
            if (!warehouseMap.containsKey(item.warehouseId())) {
                throw new WarehouseException(
                        "Kho không tồn tại để thêm vào hàng hóa " + item.warehouseId());
            }
            if (!zoneMap.containsKey(item.zoneId())) {
                throw new WarehouseZoneException(
                        "Zone không tồn tại để thêm vào hàng hóa " + item.zoneId());
            }
        }

    }

    // Hàm tạo entity order cho outbound nhưng chưa mở transaction
    private OutboundOrder buildOutboundOrder(OutboundRequest request,
                                             Customer customer,
                                             Map<Long, Product> productMap, Map<Long, WarehouseEntity> warehouseMap,
                                             Map<Long, WarehouseZone> zoneMap) {
        // Tạo thông tin cơ bản cho OutboundOrder

        OutboundOrder newOrder = new OutboundOrder();
        newOrder.setRequestId(request.requestId());
        newOrder.setCustomer(customer);
        newOrder.setOrderCode("OUT-" + UUID.randomUUID());
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setNote(request.note());

        // Tạo chi tiết sản phẩm trong order outbound
        List<OutboundOrderItems> items = request.items().stream()
                .map(item -> {
                    OutboundOrderItems currentItem = new OutboundOrderItems();
                    currentItem.setOutboundOrder(newOrder);
                    currentItem.setProduct(productMap.get(item.productId()));
                    currentItem.setQuantity(item.quantity());
                    currentItem.setWarehouse(warehouseMap.get(item.warehouseId()));
                    currentItem.setZone(zoneMap.get(item.zoneId()));
                    return currentItem;
                }).collect(Collectors.toList());

        // Trả về order sau khi đã lưu hết giá trị vào
        newOrder.setOutboundOrderItems(items);
        return newOrder;
    }

    // Hàm lưu vào database của dữ liệu (Mở Transaction)

    /**
     * Transaction scope nhỏ nhất có thể — chỉ bao phần INSERT.
     * Connection chỉ bị giữ trong ~2–5ms thay vì 20–50ms như trước.
     * <p>
     * REQUIRES_NEW để không bị ảnh hưởng nếu caller có transaction riêng.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundResponse saveNewOrder(OutboundOrder order, String requestId) {
        try {
            // lƯU order vào database
            OutboundOrder savedOrder = outboundRepository.save(order);
            log.info("Order được tạo thành công: {}", savedOrder.getId());
            return mapToResponse(savedOrder);
        } catch (Exception e) {
            // TODO: handle exception
            // Trường hợp bị race condition: vì có requestId giống nhau (unique constraint)
            log.error("Đã có order được tạo với requestId: {}", requestId);
            return outboundRepository.findByRequestId(requestId).map(this::mapToResponse)
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy order với requestId: " + requestId));

        }
    }

    @Transactional
    public OutboundResponse cancelOrder(Long orderId) { // ham xoa the outbound
        OutboundOrder order = outboundRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiếu xuất kho để hủy: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy phiếu ở trạng thái PENDING. Trạng thái hiện tại: "
                    + order.getStatus());
        }
        // Kiem tra co bi request 2 lan khong
        int success = outboundRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED, OrderStatus.PENDING);
        if (success == 0) {
            throw new RuntimeException("Đơn hàng đã bị xử lý bởi request khác");
        }

        return mapToResponse(order);
    }

    //Check quyền permission warehouse
    private void checkPermssion(OutboundRequest request, Long userId) {
        //Check trung warehouseIds
        Set<Long> warehouseIds = request.items().stream()
                .map(OutboundItemRequest::warehouseId)
                .collect(Collectors.toSet());

        //kiem tra xem permission có đúng không
        permissionWarehouseService.checkPermissionByListWarehouseId(userId, warehouseIds, PermissionCode.OUTBOUND_CREATE);
    }

    private OutboundResponse mapToResponse(OutboundOrder order) {
        // Map lồng danh sách items để trả về thông tin đầy đủ cho Frontend
        List<OutboundItemRequest> itemDTOs = order.getOutboundOrderItems().stream()
                .map(item -> new OutboundItemRequest(
                        item.getProduct().getId(),
                        item.getWarehouse().getId(),
                        item.getZone().getId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        return new OutboundResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomer().getName(),
                order.getStatus().toString(),
                order.getOrderDateTime(),
                order.getNote(),
                itemDTOs);
    }


}
