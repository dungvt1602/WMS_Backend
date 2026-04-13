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

- [x] **Phase 4: Core Inventory Logic**
    - [x] Inventory Module (add/removeStock).
    - [x] Optimistic Locking with `@Version`.
    - [x] Stock Movement Log (Chi tiết truy vết biến động hàng).

- [x] **Phase 5: Inbound & Outbound (Business Flow)**
    - [x] Create common `OrderStatus` enum.
    - [x] **Inbound Module**
    - [x] **Outbound Module**

---

### 🚀 Giai đoạn hiện tại đang làm

- [ ] **Phase 6: Tích hợp Redis Caching (Truy xuất siêu tốc)**
    - [ ] `1.` Mở file `WmsApplication.java` và bật `@EnableCaching`.
    - [ ] `2.` Tạo file cấu hình `RedisConfig.java` để ép dữ liệu cache thành định dạng JSON chuẩn.
    - [ ] `3.` Cập nhật `ProductService`: Gắn `@Cacheable` cho hàm Get và `@CacheEvict` cho hàm Create/Update/Delete.
    - [ ] `4.` Cập nhật `WarehouseService`: Gắn Annotation tương tự cho Kho hàng.

- [ ] **Phase 7: Nâng cấp Scalability (Chống sập & Trùng lặp)** *(Tùy chọn nâng cao)*
    - [ ] Ghi Log bất đồng bộ với `@Async`: Tách hàm ghi `StockMovement` sang một luồng (thread) khác để không làm chậm luồng xuất/nhập kho chính.
    - [ ] Thêm cấu hình Idempotency (Outbound): Ngăn chặn lỗi Front-end lỡ tay bấm click "Xuất kho" 2 lần liên tục gây trừ nhầm hàng 2 lần.
