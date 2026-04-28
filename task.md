# WMS Project Implementation Checklist

- [x] **Phase 1: Core Infrastructure & Shared Setup**
    - [x] Setup `BaseEntity` with JPA Auditing.
    - [x] Configure Database Connection.
    - [x] Setup Standard API Response wrapper.
    - [x] Setup Global Exception Handler.
    - [x] Setup Hibernate Auto-DDL (Database up and running).

- [x] **Phase 2: Authentication & Authorization (Security)**
    - [x] Create `User` and `Role` entities.
    - [x] Implement JWT Service and Filter.
    - [x] Implement `AuthService` and `AuthController`.

- [x] **Phase 3: Master Data Modules**
    - [x] Product Module (CRUD).
    - [x] Warehouse Module (CRUD).
    - [x] Partner Module (Customers & Suppliers).

- [x] **Phase 4: Core Inventory Logic**
    - [x] Inventory Module (add/removeStock).
    - [x] Optimistic Locking with `@Version`.
    - [x] Stock Movement Log & Transfer Logic.

- [x] **Phase 5: Inbound & Outbound (Business Flow)**
    - [x] Create common `OrderStatus` enum.
    - [x] **Inbound Module**
    - [x] **Outbound Module**

---

### 🚀 Giai đoạn hiện tại đang làm

- [x] **Phase 6: Tích hợp Redis Caching (Truy xuất siêu tốc)**
    - [x] `1.` Mở file `WmsApplication.java` và bật `@EnableCaching`.
    - [x] `2.` Tạo file cấu hình `RedisConfig.java` để ép dữ liệu cache thành định dạng JSON chuẩn.
    - [x] `3.` Cập nhật `ProductService`: Gắn `@Cacheable` cho hàm Get và `@CacheEvict` cho hàm Create/Update/Delete.
    - [x] `4.` Cập nhật `WarehouseService`: Gắn Annotation tương tự cho Kho hàng.

- [/] **Phase 7: Nâng cấp Scalability & Data Integrity (Production-grade)**
    - [x] `1.` Cấu hình `@TransactionalEventListener(phase = AFTER_COMMIT)` cho việc ghi Log.
    - [x] `2.` Tạo `StockMovementEvent`, `TransferStockEvent` và Publisher trong `InventoryService`.
    - [x] `3.` Triển khai Async Listener (`MovementTypeEvent`) lưu Log sau khi Transaction Commit thành công.
    - [x] `4.` Denormalization Audit Log: Lưu `productName`, `warehouseName`, `userName` trực tiếp vào bảng log.
    - [x] `5.` Tạo `InventoryLog` Entity với DB Constraint `UNIQUE(order_code, product_id)` — Final Gate chống trùng dữ liệu.
    - [x] `6.` Atomic Update (`updateOrderStatus`): JPQL `UPDATE ... WHERE status = :oldStatus` cho Inbound & Outbound.
    - [x] `7.` Idempotency cho Create Order: `requestId` + `UNIQUE constraint` trên `OutboundOrder`.
    - [x] `8.` Outbox Pattern: Tạo module `infrastructure/outbox` lưu event trước khi gửi Kafka.
    - [x] `9.` Redis Lua Script: Atomic decrease/increase stock + `processedKey` chống trùng lệnh cho Hot Item.
    - [x] `10.` 2-Phase Reserve + Commit (`OutboundCompleteService`): Reserve ngoài TX → Commit trong TX ngắn.
    - [x] `11.` Compensating Transaction: `rollbackReservedItems()` hoàn trả cả Redis lẫn DB khi fail giữa chừng.
    

- [x] **Phase 8: Quản lý trạng thái & Hủy phiếu (Order Life-cycle)** 🚀
    - [x] Thêm trạng thái `CANCELLED` vào `OrderStatus`.
    - [x] Triển khai hàm `cancelOrder` cho Inbound (PENDING -> CANCELLED).
    - [x] Triển khai hàm `cancelOrder` cho Outbound (PENDING -> CANCELLED).

[//]: # (- [ ] **Phase 9: Quản lý Vận chuyển &#40;Shipping & Carrier&#41;** 🚀)

[//]: # (    - [ ] Tạo module `Carrier` quản lý đơn vị vận chuyển &#40;GHTK, Viettel Post, v.v.&#41;.)

[//]: # (    - [ ] Gắn thông tin vận chuyển vào phiếu Xuất kho &#40;`trackingNumber`, `shippingFee`&#41;.)

- [x] **Phase 10: Hệ thống Cảnh báo & Phân quyền nâng cao**

[//]: # (    - [ ] Low Stock Alert &#40;Cảnh báo hàng dưới mức tối thiểu&#41;.)
    - [x] Phân quyền chi tiết theo từng Kho (Warehouse-level RBAC).

- [x] **Phase 11: Báo cáo & Thống kê (Reports & Dashboard)**
    - [x] Hoàn thiện `InventoryReportService`.
    - [x] API Dashboard tổng hợp.

- [ ] **Phase 12: Test Stability & Release Gate**
    - [ ] Chạy pass `mvn test` toàn bộ project.
    - [ ] Bổ sung integration test cho luồng Inbound/Outbound/Transfer.
    - [ ] Bổ sung test permission theo kho (allow/deny theo role + warehouse).
    - [ ] Bổ sung test Outbox Dispatcher + retry/dead flow.

- [ ] **Phase 13: Database Migration & Data Safety**
    - [ ] Tích hợp `Flyway` hoặc `Liquibase`.
    - [ ] Tắt `ddl-auto=update` ở môi trường production.
    - [ ] Chuẩn hóa index/unique constraint cho bảng lớn (`inv_`, `outbox_`, `auth_` mapping).
    - [ ] Thiết kế và kiểm thử backup/restore định kỳ.

- [ ] **Phase 14: Security Hardening**
    - [ ] Đưa JWT secret/config nhạy cảm ra env/secret manager.
    - [ ] Thiết lập chính sách JWT TTL + rotation.
    - [ ] Thêm rate limit cho endpoint auth/login.
    - [ ] Audit log cho thao tác admin (grant/revoke permission).

- [ ] **Phase 15: Event Reliability & Idempotency**
    - [ ] Giám sát và xử lý `outbox DEAD` (manual replay/re-drive).
    - [ ] Rà soát idempotency end-to-end cho consumer.
    - [ ] Chuẩn hóa retry/backoff cho producer/consumer Kafka.
    - [ ] Viết runbook xử lý lệch tồn kho do event fail.

- [ ] **Phase 16: Observability & Monitoring**
    - [ ] Metrics ứng dụng: latency, error rate, throughput.
    - [ ] Metrics hạ tầng: DB pool, Kafka lag, Redis hit ratio.
    - [ ] Structured logging + `traceId/correlationId`.
    - [ ] Alerting cho low stock, outbox dead, consumer failure, DB saturation.

- [ ] **Phase 17: Deployment & Go-Live Readiness**
    - [ ] Tách profile `dev/staging/prod` và externalize config.
    - [ ] CI/CD pipeline: build, test, scan, migrate, smoke test.
    - [ ] Chuẩn hóa chiến lược rollback release.
    - [ ] Hoàn thiện runbook vận hành và incident response.
