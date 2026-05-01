# 📦 WMS Backend — Warehouse Management System

> Production-grade backend xử lý nghiệp vụ kho hàng: Nhập/Xuất/Chuyển kho, Tồn kho realtime, Phân quyền theo kho, Event-Driven Architecture.

## Tech Stack

| Layer | Công nghệ |
|-------|----------|
| **Core** | Java 21 (Virtual Threads), Spring Boot 3.5 |
| **Database** | PostgreSQL, JPA/Hibernate, Flyway |
| **Cache** | Redis (Caching + Lua Script atomic operations) |
| **Messaging** | Apache Kafka (Event-Driven inventory updates) |
| **Security** | JWT + BCrypt, RBAC + Warehouse-level Permission |
| **Infra** | Docker Compose, Oracle Kubernetes Engine (OKE), Jenkins CI/CD |

---

## Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        Spring Boot App                          │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐  ┌──────────────┐  │
│  │   Auth   │  │ Inbound  │  │ Outbound  │  │  Inventory   │  │
│  │  (JWT)   │  │          │  │           │  │              │  │
│  └──────────┘  └──────────┘  └─────┬─────┘  └──────┬───────┘  │
│                                    │               │           │
│                              ┌─────▼───────────────▼─────┐     │
│                              │   Infrastructure Module   │     │
│                              │  ┌────────┐  ┌─────────┐  │     │
│                              │  │ Outbox │  │  Redis  │  │     │
│                              │  └───┬────┘  │  Cache  │  │     │
│                              │      │       └─────────┘  │     │
│                              └──────┼────────────────────┘     │
└─────────────────┼───────────────────┼──────────────────────────┘
                  │                   │
           ┌──────▼──────┐     ┌──────▼──────┐
           │ PostgreSQL  │     │    Kafka    │
           │             │     │             │
           └─────────────┘     └──────┬──────┘
                                      │
                               ┌──────▼──────┐
                               │  Consumer   │
                               │ (Idempotent)│
                               └─────────────┘
```

**Mô hình: Modular Monolith** — Deploy 1 ứng dụng, nhưng code tách biệt theo domain. Sẵn sàng tách microservice khi cần scale.

> Chi tiết kiến trúc + workflow diagram: [`ARCHITECTURE.md`](ARCHITECTURE.md)

---

## Tính năng nổi bật

### 🔐 Security & Permission
- **JWT Authentication** với TTL + rotation policy
- **RBAC 3 tầng**: Admin → Staff → Viewer
- **Warehouse-level Permission**: Staff chỉ thao tác được trên kho được phân công
- Rate limiting cho endpoint auth/login

### 📦 Inventory Management
- Tồn kho quản lý theo bộ 3: `warehouse_id + product_id + zone_id`
- **Số dư kép**: `quantity` (thực tế) + `availableQuantity` (khả dụng)
- **Pessimistic Locking** (`SELECT FOR UPDATE`) cho Nhập/Xuất/Chuyển kho
- **Deadlock Prevention**: Lock ordering theo key `WH:ZONE:PRODUCT` khi chuyển kho

### ⚡ High-Performance Outbound (Hot Item)
- **2-Phase Reserve + Commit**: Reserve ngoài transaction → Commit trong transaction ngắn (~3ms)
- **Redis Lua Script**: Trừ tồn kho atomic cho hàng hot, kèm `processedKey` chống trùng lệnh
- **Compensating Transaction (Saga)**: Rollback cả Redis lẫn DB khi fail giữa chừng

### 🛡️ Data Integrity & Idempotency
- **Atomic Update**: `UPDATE ... WHERE status = :expected` chống double-click
- **Idempotency Key**: `requestId` + DB Unique Constraint cho Create Order
- **Outbox Pattern**: Event và Order commit cùng 1 transaction — không bao giờ mất event
- **Kafka Consumer Idempotency**: 2 lớp bảo vệ (code check + DB `UNIQUE(order_code, product_id)`)

### 📊 Audit & Reporting
- **Event-Driven Audit Log**: `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`
- **Denormalized Log**: Lưu `productName`, `warehouseName` trực tiếp — query nhanh, không cần JOIN
- API báo cáo: Inventory Summary, Stock History, Transfer History (paginated)

---

## Modules

| Module | Mô tả | File chính |
|--------|-------|------------|
| `auth` | JWT, RBAC, Warehouse Permission | `AuthService`, `JwtService` |
| `product` | CRUD + Redis Cache | `ProductService` |
| `warehouse` | Warehouse + Zone + Redis Cache | `WarehouseService` |
| `customer` | Khách hàng B2B/B2C + Nhà cung cấp | `CustomerService` |
| `inventory` | Stock, Movement, Transfer, Lock | `InventoryService` |
| `inbound` | Phiếu nhập kho | `InboundService` |
| `outbound` | Phiếu xuất kho (Hot/Cold item) | `OutboundCompleteService` |
| `infrastructure` | Outbox Pattern, Redis Config | `OutboxDispatcher` |

---

## Concurrency & Locking Strategy

```
┌─────────────────────────────────────────────────────┐
│              Chiến lược xử lý đồng thời             │
├─────────────────┬───────────────────────────────────┤
│ Pessimistic     │ Nhập/Xuất/Chuyển kho              │
│ Lock            │ SELECT ... FOR UPDATE              │
├─────────────────┼───────────────────────────────────┤
│ Optimistic      │ Product, Warehouse (ít write)      │
│ Lock            │ @Version column                    │
├─────────────────┼───────────────────────────────────┤
│ Atomic Update   │ Order status transition             │
│                 │ UPDATE WHERE status = :expected     │
├─────────────────┼───────────────────────────────────┤
│ Redis Atomic    │ Hot item stock (Lua Script)         │
│                 │ DECRBY + processedKey check         │
├─────────────────┼───────────────────────────────────┤
│ Deadlock        │ Transfer: lock theo thứ tự key      │
│ Prevention      │ WH:ZONE:PRODUCT ascending           │
└─────────────────┴───────────────────────────────────┘
```

---

## Database Schema

Tên bảng theo prefix module:

| Prefix | Module | Bảng tiêu biểu |
|--------|--------|----------------|
| `auth_` | Auth | `auth_user`, `auth_role`, `auth_user_warehouse` |
| `prd_` | Product | `prd_product`, `prd_category` |
| `wh_` | Warehouse | `wh_warehouse`, `wh_zone` |
| `inv_` | Inventory | `inv_inventory`, `inv_stock_movement`, `inv_inventory_log` |
| `inb_` | Inbound | `inb_inbound_order`, `inb_inbound_items` |
| `out_` | Outbound | `out_outbound_order`, `out_outbound_items` |
| `outbox_` | Infrastructure | `outbox_events` |

> ERD: [`/docs/erd.png`](/docs/erd.png)

### Database Migration (Flyway)

Schema được quản lý hoàn toàn bằng **Flyway** — không dùng `ddl-auto=update`.

```
src/main/resources/db/migration/
├── V1__baseline_schema.sql      ← Full pg_dump (59KB): tất cả bảng + constraint + seed data
└── V2__missingindex_add.sql     ← 15 index cho các bảng hot: inventory, movement, auth, order
```

| Cấu hình | Giá trị | Lý do |
|----------|---------|-------|
| `ddl-auto` | `validate` | Hibernate chỉ kiểm tra, không tự sửa schema |
| `flyway.enabled` | `true` | Bật migration tự động khi khởi động |
| `flyway.clean-disabled` | `true` | Cấm drop toàn bộ DB (an toàn cho prod) |
| `flyway.out-of-order` | `false` | V2 không được chạy trước V1 |
| `flyway.validate-on-migrate` | `true` | Checksum phát hiện file migration bị sửa |

---

## Cài đặt & Chạy

```bash
# 1. Clone
git clone https://github.com/dungvt1602/WMS_Backend.git
cd wms

# 2. Khởi động hạ tầng (PostgreSQL, Redis, Kafka)
docker compose up -d

# 3. Cấu hình environment variables
# JWT_SECRET, JWT_EXPIRATION, DB_USERNAME, DB_PASSWORD

# 4. Chạy ứng dụng
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Ứng dụng chạy tại: `http://localhost:8080`

---

## Deployment

| Component | Platform |
|-----------|----------|
| Backend | Oracle Kubernetes Engine (OKE) |
| CI/CD | Jenkins (build → test → deploy) |
| Secrets | HashiCorp Vault |
| Frontend | Vercel |

> Chi tiết triển khai: [`deploy.md`](deploy.md)
> Quản lý secrets: [`docs/security-secrets.md`](docs/security-secrets.md)

---

## Tác giả

**Nguyễn Tiến Dũng** — Backend Developer

Dự án được xây dựng với mục tiêu rèn luyện kỹ năng backend từ Junior lên Mid-level, tập trung vào: Transaction management, Concurrency control, Event-Driven Architecture, và Production deployment.
