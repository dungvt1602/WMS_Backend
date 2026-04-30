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
    - [x] `4.` Cập nhật `WarehouseService`: Gắn annotation tương tự cho kho hàng.

- [/] **Phase 7: Nâng cấp Scalability & Data Integrity (Production-grade)**
    - [x] `1.` Cấu hình `@TransactionalEventListener(phase = AFTER_COMMIT)` cho việc ghi log.
    - [x] `2.` Tạo `StockMovementEvent`, `TransferStockEvent` và publisher trong `InventoryService`.
    - [x] `3.` Triển khai async listener (`MovementTypeEvent`) lưu log sau khi transaction commit thành công.
    - [x] `4.` Denormalization audit log: lưu `productName`, `warehouseName`, `userName` trực tiếp vào bảng log.
    - [x] `5.` Tạo `InventoryLog` entity với DB constraint `UNIQUE(order_code, product_id)` - final gate chống trùng dữ liệu.
    - [x] `6.` Atomic update (`updateOrderStatus`): JPQL `UPDATE ... WHERE status = :oldStatus` cho Inbound & Outbound.
    - [x] `7.` Idempotency cho create order: `requestId` + `UNIQUE constraint` trên `OutboundOrder`.
    - [x] `8.` Outbox pattern: tạo module `infrastructure/outbox` lưu event trước khi gửi Kafka.
    - [x] `9.` Redis Lua script: atomic decrease/increase stock + `processedKey` chống trùng lệnh cho hot item.
    - [x] `10.` 2-phase reserve + commit (`OutboundCompleteService`): reserve ngoài TX -> commit trong TX ngắn.
    - [x] `11.` Compensating transaction: `rollbackReservedItems()` hoàn trả cả Redis lẫn DB khi fail giữa chừng.

- [x] **Phase 8: Quản lý trạng thái & Hủy phiếu (Order Life-cycle)**
    - [x] Thêm trạng thái `CANCELLED` vào `OrderStatus`.
    - [x] Triển khai hàm `cancelOrder` cho Inbound (PENDING -> CANCELLED).
    - [x] Triển khai hàm `cancelOrder` cho Outbound (PENDING -> CANCELLED).

[//]: # (- [ ] **Phase 9: Quản lý Vận chuyển (Shipping & Carrier)** 🚀)
[//]: # (    - [ ] Tạo module `Carrier` quản lý đơn vị vận chuyển (GHTK, Viettel Post, v.v.).)
[//]: # (    - [ ] Gắn thông tin vận chuyển vào phiếu xuất kho (`trackingNumber`, `shippingFee`).)

- [x] **Phase 10: Hệ thống Cảnh báo & Phân quyền nâng cao**
[//]: # (    - [ ] Low Stock Alert (Cảnh báo hàng dưới mức tối thiểu).)
    - [x] Phân quyền chi tiết theo từng kho (Warehouse-level RBAC).

- [x] **Phase 11: Báo cáo & Thống kê (Reports & Dashboard)**
    - [x] Hoàn thiện `InventoryReportService`.
    - [x] API dashboard tổng hợp.

- [ ] **Phase 12: Test Stability & Release Gate**
    - [ ] Chạy pass `mvn test` toàn bộ project.
[//]: # (    - [ ] Bổ sung integration test cho luồng Inbound/Outbound/Transfer.)
[//]: # (    - [ ] Bổ sung test permission theo kho (allow/deny theo role + warehouse).)
[//]: # (    - [ ] Bổ sung test Outbox Dispatcher + retry/dead flow.)

- [ ] **Phase 13: Database Migration & Data Safety**
    - [ ] Tích hợp `Flyway` hoặc `Liquibase`.
    - [ ] Tắt `ddl-auto=update` ở môi trường production.
    - [ ] Chuẩn hóa index/unique constraint cho bảng lớn (`inv_`, `outbox_`, `auth_` mapping).
    - [ ] Thiết kế và kiểm thử backup/restore định kỳ.

- [ ] **Phase 14: Security Hardening**
    - [/] Đưa JWT secret/config nhạy cảm ra env/secret manager. (Code/config đã sẵn sàng, runtime secret manager cho prod: pending)
    - [x] Thiết lập chính sách JWT TTL + rotation.
    - [x] Thêm rate limit cho endpoint auth/login.
    - [x] Audit log cho thao tác admin (grant/revoke permission).

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
    - [ ] Triển khai theo `deploy.md` (checklist chi tiết):
    - [ ] P17.1.1 Viết Dockerfile + test build local.
    - [ ] P17.1.2 Thêm/kiểm tra `/actuator/health`.
    - [ ] P17.1.3 Push code lên GitHub.
    - [ ] P17.1.4 Tạo Oracle Cloud account.
    - [ ] P17.2.1 Tạo OKE Cluster (Always Free ARM).
    - [ ] P17.2.2 Tạo Jenkins VM (Always Free AMD).
    - [ ] P17.2.3 Cài Jenkins + Docker + kubectl.
    - [ ] P17.2.4 Kết nối Jenkins kubectl -> OKE.
    - [ ] P17.3.1 Tạo `k8s/namespace.yaml`.
    - [ ] P17.3.2 Tạo `k8s/configmap.yaml`.
    - [ ] P17.3.3 Tạo `k8s/secret.yaml` (tạm thời, chưa Vault).
    - [ ] P17.3.4 Tạo `k8s/deployment.yaml`.
    - [ ] P17.3.5 Tạo `k8s/service.yaml`.
    - [ ] P17.3.6 `kubectl apply` + verify pod/service/public API.
    - [ ] P17.4.1 Viết `Jenkinsfile`.
    - [ ] P17.4.2 Tạo Jenkins pipeline job + credentials registry.
    - [ ] P17.4.3 Cấu hình GitHub webhook.
    - [ ] P17.4.4 Test flow push code -> build -> deploy.
    - [ ] P17.5.1 Cài Vault trên OKE bằng Helm.
    - [ ] P17.5.2 Init + unseal Vault.
    - [ ] P17.5.3 Lưu WMS secrets vào Vault.
    - [ ] P17.5.4 Cấu hình Spring Boot đọc secret từ Vault.
    - [ ] P17.5.5 Gỡ K8s secret thủ công, chuyển sang Vault.
    - [ ] P17.6.1 Cấu hình CORS cho domain frontend.
    - [ ] P17.6.2 Set `VITE_API_BASE_URL` trên Vercel.
    - [ ] P17.6.3 Test E2E Frontend -> Backend -> DB.
