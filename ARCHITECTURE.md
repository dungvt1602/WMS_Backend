# 🏗️ Kiến Trúc Hệ Thống WMS (ARCHITECTURE)

## 1. Kiến trúc tổng quát (High-Level Architecture)

Hệ thống WMS được thiết kế theo mô hình **Modular Monolith**.

Trong mô hình này, toàn bộ hệ thống được triển khai dưới dạng một ứng dụng duy nhất, nhưng code được chia thành nhiều module theo domain nhằm đảm bảo tính tách biệt logic.

**Các module chính của hệ thống:**

- **Auth**: Quản lý xác thực (JWT) và phân quyền (RBAC).
- **Product**: Quản lý thông tin sản phẩm và Danh mục (Category).
- **Warehouse**: Quản lý kho bãi và các Phân vùng (WarehouseZone).
- **Customer**: Quản lý đối tác (Khách hàng B2B/B2C và Nhà cung cấp).
- **Inventory**: Trái tim của hệ thống, quản lý tồn kho tại từng Zone.
- **Inbound**: Xử lý quy trình nhập hàng.
- **Outbound**: Xử lý quy trình xuất hàng (hỗ trợ hàng Hot qua Redis).

**Luồng dữ liệu tổng quát:**

```mermaid
graph TD
    Client --> API_Gateway[Spring Boot Controller]
    API_Gateway --> Service_Layer[Module Service]
    Service_Layer --> Redis[(Redis Cache/Script)]
    Service_Layer --> DB[(PostgreSQL)]
    Service_Layer --> Kafka{{Kafka Message Queue}}
    Kafka --> InventoryConsumer[Inventory Consumer]
    InventoryConsumer --> DB
```

---

## 2. Quản lý Tồn kho & Đa phân vùng (Multi-zone Support)

Hệ thống hỗ trợ quản lý tồn kho chi tiết đến từng vị trí (Zone/Rack/Bin) trong kho.

### Cấu trúc dữ liệu Tồn kho
Một bản ghi tồn kho (`Inventory`) là sự kết hợp duy nhất của:
- **WarehouseId** + **ProductId** + **ZoneId**

### Nghiệp vụ Chuyển kho (Inventory Transfer)
Cho phép di chuyển hàng hóa giữa các Zone hoặc giữa các Kho khác nhau. Luồng này đảm bảo tính nguyên tử (Atomic) bằng cách sử dụng `@Transactional` và khóa hàng tại cả hai đầu nguồn/đích.

```mermaid
sequenceDiagram
    participant S as Source Zone
    participant T as Target Zone
    Note over S,T: Khóa bi quan nguồn & đích
    S->>S: Trừ số lượng
    T->>T: Cộng số lượng
    Note over S,T: Lưu Log Movement
```

---

## 3. Xử lý đồng thời & Khóa (Concurrency & Locking)

Hệ thống áp dụng chiến lược khóa kép để đảm bảo tính chính xác tuyệt đối của số lượng tồn kho.

### Pessimistic Locking (Khóa bi quan)
Được sử dụng trong các nghiệp vụ Nhập/Xuất/Chuyển kho để tránh tình trạng nhiều tiến trình cùng cập nhật một bản ghi dẫn đến sai lệch số dư.
- Sử dụng cú pháp: `SELECT ... FOR UPDATE` thông qua `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

> [!IMPORTANT]
> **Cơ chế phòng chống Deadlock:** 
> Khi thực hiện Chuyển kho (Transfer), hệ thống luôn thực hiện khóa các bản ghi theo thứ tự định danh (ID) tăng dần. Điều này triệt tiêu vòng lặp chờ chéo giữa các tiến trình chuyển hàng ngược chiều nhau.

### Optimistic Locking (Khóa lạc quan)
Vẫn được duy trì qua trường `@Version` để bảo vệ các dữ liệu ít biến động như thông tin Sản phẩm hoặc Kho.

---

## 4. Cơ chế giao tiếp & Hiệu năng

### Decoupling với Kafka
Để tăng tốc tối đa cho luồng Xuất kho (Outbound), hệ thống không cập nhật Database trực tiếp trong luồng chính của Controller. 
1. `OutboundService` đẩy một `InventoryEvent` vào Kafka.
2. `InventoryConsumer` sẽ lắng nghe và cập nhật DB một cách bất đồng bộ.

### High-Performance với Redis
Các mặt hàng có tần suất giao dịch cực cao (Hot Items) sẽ được:
- **Warmup**: Nạp sẵn vào Redis khi khởi động App.
- **Atomic Update**: Sử dụng LUA Script để trừ tồn kho trên Redis một cách an toàn mà không cần khóa Database ngay lập tức.

---

## 5. Chiến lược dữ liệu (Data Management Strategy)

Tên bảng được đặt theo **prefix module**:

| Prefix | Module | Bảng tiêu biểu |
|--------|--------|----------------|
| `auth_` | Auth | `auth_user`, `auth_role` |
| `prd_` | Product | `prd_product`, `prd_category` |
| `wh_` | Warehouse | `wh_warehouse`, `wh_zone` |
| `cust_` | Customer | `cust_customer` |
| `inv_` | Inventory | `inv_inventory`, `inv_stock_movement` |

---

## 6. Hạ tầng & Triển khai

Hệ thống chạy trên nền tảng Docker với bộ 3: **Spring Boot + PostgreSQL + Redis + Kafka**.

```yaml
services:
  app: # Java 21 Backend
  postgres: # Relational DB
  redis: # Cache & Hot Stock
  kafka: # Event Streaming
  zookeeper: # Kafka Manager
```

---

# WMS Outbound — Complete Workflow Documentation

> Tài liệu này tổng hợp toàn bộ workflow của Outbound Service bao gồm: createOrder, completeOrder, Outbox Pattern và Kafka Consumer idempotency.

---

## Mục lục

1. [Kiến trúc tổng quan](#1-kiến-trúc-tổng-quan)
2. [createOrder](#2-createorder)
3. [completeOrder — 3 Phase](#3-completeorder--3-phase)
4. [Outbox Pattern](#4-outbox-pattern)
5. [Kafka Consumer + Idempotency](#5-kafka-consumer--idempotency)
6. [DB Pool & Transaction Rules](#6-db-pool--transaction-rules)
7. [Checklist Production](#7-checklist-production)

---

## 1. Kiến trúc tổng quan

```
HTTP Request
    │
    ▼
OutboundController
    │
    ├──▶ OutboundOrderService       (createOrder)
    │
    └──▶ OutboundCompleteService    (completeOrder)
              │
              ├── InventoryRedisService   (hot item — Redis)
              ├── InventoryService        (cold item — DB)
              ├── OutboxInventoryService  (ghi outbox_events)
              │
              └── [sau commit] OutboxDispatcher
                                    │
                                    └──▶ Kafka topic: inventory-events
                                                    │
                                                    └──▶ InventoryConsumer
                                                              │
                                                              ├── removeStock()
                                                              └── inventory_log (idempotency)
```

### Package structure

```
com.project.wms
├── outbound
│   ├── controller
│   ├── service
│   │   ├── OutboundOrderService.java        ← createOrder
│   │   └── OutboundCompleteService.java     ← completeOrder
│   ├── dto
│   │   └── ReservedItem.java
│   ├── entity
│   └── repository
│
├── inventory
│   ├── service
│   │   ├── InventoryService.java
│   │   └── InventoryRedisService.java
│   ├── kafka
│   │   ├── InventoryEvent.java
│   │   ├── InventoryProducer.java
│   │   └── InventoryConsumer.java           ← idempotency check
│   └── entity
│       └── InventoryLog.java                ← bảng idempotency
│
└── infrastructure                           ← dùng chung mọi module
    └── outbox
        ├── OutboxEvent.java
        ├── OutboxStatus.java
        ├── OutboxRepository.java
        ├── OutboxService.java
        └── OutboxDispatcher.java            ← @Scheduled mỗi 5s
```

---

## 2. createOrder

### Luồng

```mermaid
flowchart TD
    A([HTTP POST /outbound/orders]) --> B[Validate requestId]
    B --> C{requestId blank?}
    C -- có --> D[throw 400]
    C -- không --> E[findByRequestId\n1 query ngoài tx]
    E --> F{đã tồn tại?}
    F -- có --> G[return response cũ\nidempotency]
    F -- không --> H[findById customerId\n1 query ngoài tx]
    H --> I{customer tồn tại\nvà type=CUSTOMER?}
    I -- không --> J[throw 404 / 400]
    I -- có --> K[findAllById products\nfindAllById warehouses\nbatch 2 query ngoài tx]
    K --> L[validateItems\nin-memory không cần DB]
    L --> M{item hợp lệ?}
    M -- không --> N[throw 400]
    M -- có --> O[buildOutboundOrder\nin-memory]
    O --> P["@Transactional REQUIRES_NEW\nsaveOrderTransactional()"]
    P --> Q[outboundRepository.save]
    Q --> R{DataIntegrityViolation?}
    R -- có race condition --> S[findByRequestId\ntrả kết quả cũ]
    R -- không --> T[DB COMMIT ✓]
    T --> U([return OutboundResponse])

    style D fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style J fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style N fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style G fill:#EAF3DE,stroke:#3B6D11,color:#3B6D11
    style T fill:#E6F1FB,stroke:#185FA5,color:#185FA5
    style U fill:#EAF3DE,stroke:#3B6D11,color:#3B6D11
```

### Rules quan trọng

| Rule | Lý do |
|---|---|
| Không `@Transactional` ở `createOrder()` | Tránh giữ connection khi validate |
| Dùng `customerId` thay `customerName` | PK index nhanh hơn full scan |
| Batch load products + warehouses | Tránh N+1 query |
| `buildOutboundOrder()` in-memory | Build object trước, không cần DB |
| `REQUIRES_NEW` chỉ bao `save()` | Transaction ngắn ~3ms |
| `orderCode = "OUT-" + UUID` | Không có space, unique multi-server |

---

## 3. completeOrder — 3 Phase

### Phase 1 — Validate + Reserve (ngoài transaction)

```mermaid
flowchart TD
    A([completeOrder orderId, userId]) --> B[findById orderId\nngoài transaction]
    B --> C{order tồn tại?}
    C -- không --> D[throw 404]
    C -- có --> E{status == PENDING?}
    E -- không --> F[throw 400]
    E -- có --> G[reserveAllItems]

    subgraph LOOP [for each item]
        G --> H{hotItem?}
        H -- hot --> I[decreaseStockAtomic\nRedis atomic]
        I --> J{Redis ok?}
        J -- fail --> K[rollback prev items\n+ throw]
        J -- ok --> L[reservedItems.add\nhot item]
        H -- cold --> M[removeStock\nDB với lock]
        M --> N{DB ok?}
        N -- fail --> O[rollback prev items\n+ throw]
        N -- ok --> P[reservedItems.add\ncold item]
        L --> Q{còn item?}
        P --> Q
        Q -- có --> H
    end

    Q -- hết --> R[Phase 2]

    style D fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style F fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style K fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style O fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
```

### Phase 2 — Commit DB (transaction ngắn ~3ms)

```mermaid
flowchart TD
    A([Phase 2 — @Transactional REQUIRES_NEW]) --> B[updateOrderStatus\nPENDING → COMPLETED\noptimistic lock]
    B --> C{updated == 0?}
    C -- race condition --> D[rollbackReservedItems\nhot: Redis +\ncold: DB +\n+ throw]
    C -- ok --> E[for each reservedItem\nhot + cold]
    E --> F[outboxInventoryService\n.saveInventoryOutboundEvent\nINSERT outbox_events\ncùng transaction]
    F --> G[DB COMMIT ✓\nUPDATE order + INSERT outbox\ncùng lúc]
    G --> H[findById reload order]
    H --> I[mapToResponse]
    I --> J([return OutboundResponse\nHTTP 200 — không chờ Kafka])

    style D fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style G fill:#E6F1FB,stroke:#185FA5,color:#185FA5
    style J fill:#EAF3DE,stroke:#3B6D11,color:#3B6D11
```

### Phase 3 — OutboxDispatcher (sau commit, độc lập)

```mermaid
flowchart TD
    A([OutboxDispatcher\n@Scheduled fixedDelay=5000]) --> B[SELECT top 100\nWHERE status=PENDING\nORDER BY created_at ASC]
    B --> C{có rows?}
    C -- không --> D[sleep 5s]
    C -- có --> E[for each event]
    E --> F[sendInventoryEvent\nKafka]
    F --> G{gửi ok?}
    G -- ok --> H[UPDATE status=SENT\nsent_at=now]
    G -- fail --> I{retryCount >= 5?}
    I -- không --> J[retryCount++\ngiữ PENDING\nthử lại lần sau]
    I -- có --> K[status=DEAD\nlog ERROR\ncần xử lý thủ công]
    H --> L{còn event?}
    J --> L
    K --> L
    L -- có --> E
    L -- hết --> M([done — sleep 5s])

    style K fill:#FCEBEB,stroke:#A32D2D,color:#A32D2D
    style M fill:#EAF3DE,stroke:#3B6D11,color:#3B6D11
```

### Rollback logic

```mermaid
flowchart TD
    A([rollbackReservedItems]) --> B[for each item trong reservedItems]
    B --> C{item.isHot?}
    C -- hot --> D[inventoryRedisService\n.increaseStock\nhoàn lại Redis]
    C -- cold --> E[inventoryService\n.addStock\nhoàn lại DB]
    D --> F{còn item?}
    E --> F
    F -- có --> B
    F -- hết --> G([done])

    D -- exception --> H[log ERROR\nCẦN XỬ LÝ THỦ CÔNG\nkhông throw\ntiếp tục rollback]
    E -- exception --> H
    H --> F
```

---

## 4. Outbox Pattern

### Tại sao cần?

| Vấn đề | Không có Outbox | Có Outbox |
|---|---|---|
| App crash sau DB commit | Event mất vĩnh viễn | Event còn trong DB, retry tự động |
| Kafka down tạm thời | Event mất | Dispatcher retry khi Kafka up lại |
| 2 instance gửi cùng lúc | Consumer nhận 2 lần | Consumer idempotency xử lý |

### Điểm mấu chốt

```
@Transactional
commitOrder() {
    UPDATE outbound_orders SET status=COMPLETED    ─┐
    INSERT outbox_events (status=PENDING)           ─┘ cùng 1 transaction
}
// Hai việc commit cùng nhau hoặc rollback cùng nhau
// Không bao giờ có trường hợp order=COMPLETED nhưng không có outbox event
```

### outbox_events schema

```sql
CREATE TABLE outbox_events (
    id           BIGSERIAL PRIMARY KEY,
    event_type   VARCHAR(100)  NOT NULL,  -- OUTBOUND_HOT | OUTBOUND_COLD
    payload      TEXT          NOT NULL,  -- JSON của InventoryEvent
    topic        VARCHAR(200)  NOT NULL,
    order_code   VARCHAR(100)  NOT NULL,
    status       VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    sent_at      TIMESTAMP,
    retry_count  INT           NOT NULL DEFAULT 0,
    last_error   TEXT
);

-- Index quan trọng — dispatcher query mỗi 5 giây
CREATE INDEX idx_outbox_pending
    ON outbox_events(status, created_at)
    WHERE status = 'PENDING';
```

### OutboxStatus flow

```
PENDING ──▶ SENT      (gửi Kafka thành công)
        ──▶ PENDING   (gửi fail, retryCount < 5 → thử lại)
        ──▶ DEAD      (retryCount >= 5 → cần xử lý thủ công)
```

---

## 5. Kafka Consumer + Idempotency

### Tại sao cần idempotency ở Consumer?

```
Tình huống: Dispatcher gửi Kafka OK → crash trước khi UPDATE status=SENT
→ 5 giây sau Dispatcher chạy lại → gửi lại event
→ Consumer nhận event 2 lần → trừ kho 2 lần ✗
```

### Idempotency key

```
orderCode một mình  ✗  →  1 order nhiều items → conflict
orderCode + productId ✓  →  mỗi item trong order là duy nhất
```

Ví dụ:
```
Order OUT-abc có 3 items:
  OUT-abc + productId=1  →  unique key 1
  OUT-abc + productId=2  →  unique key 2
  OUT-abc + productId=7  →  unique key 3
```

### inventory_log schema

```sql
CREATE TABLE inventory_log (
    id            BIGSERIAL PRIMARY KEY,
    order_code    VARCHAR(100) NOT NULL,
    product_id    BIGINT       NOT NULL,
    quantity      INT          NOT NULL,
    movement_type VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- Lớp bảo vệ cuối — DB enforce dù Consumer code bị race condition
    CONSTRAINT uq_inventory_log_order_product
        UNIQUE (order_code, product_id)
);
```

### Consumer flow

```mermaid
flowchart TD
    A([Kafka message đến\nInventoryEvent]) --> B{movementType\n== OUTBOUND_HOT?}
    B -- OUTBOUND_COLD --> C[bỏ qua\ncold đã trừ DB ở Phase 1]
    B -- OUTBOUND_HOT --> D[existsByOrderCodeAndProductId\norderCode + productId]
    D --> E{đã tồn tại\ntrong inventory_log?}
    E -- có --> F[log WARN\nskip duplicate]
    E -- không --> G["@Transactional\nremoveStock hot item"]
    G --> H[INSERT inventory_log\ncùng transaction với removeStock]
    H --> I{DataIntegrityViolation?\nrace condition}
    I -- có --> J[log WARN\nskip — DB đã bảo vệ]
    I -- không --> K[COMMIT ✓\ntrừ kho + ghi log\ncùng lúc]

    style C fill:#EAF3DE,stroke:#3B6D11,color:#3B6D11
    style F fill:#FAEEDA,stroke:#854F0B,color:#854F0B
    style J fill:#FAEEDA,stroke:#854F0B,color:#854F0B
    style K fill:#E6F1FB,stroke:#185FA5,color:#185FA5
```

### 2 lớp bảo vệ idempotency

| Lớp | Cơ chế | Xử lý |
|---|---|---|
| **1. Code check** | `existsByOrderCodeAndProductId` | Skip trước khi mở transaction |
| **2. DB constraint** | `UNIQUE (order_code, product_id)` | Catch `DataIntegrityViolationException` |

---

## 6. DB Pool & Transaction Rules

### HikariCP config (application.properties)

```properties
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=3000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=5000
spring.datasource.hikari.pool-name=WMS-HikariPool
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.batch_size=25
```

### Transaction scope rules

```
createOrder()          ← KHÔNG @Transactional
  ├── validate          ngoài tx — không giữ connection
  ├── batch load        ngoài tx — không giữ connection
  └── saveOrder()      @Transactional REQUIRES_NEW — chỉ 1 INSERT ~3ms

completeOrder()        ← KHÔNG @Transactional
  ├── validate          ngoài tx
  ├── reserveAllItems   ngoài tx — Redis + DB lock
  └── commitOrder()    @Transactional REQUIRES_NEW — 1 UPDATE + N INSERT ~5ms
```

### @Transactional trên private method — KHÔNG có tác dụng

```java
// SAI — Spring AOP không proxy được private method
@Transactional
private OutboundResponse commitOrder(...) { }

// ĐÚNG — phải public
@Transactional
public OutboundResponse commitOrder(...) { }
```

---

## 7. Checklist Production

### createOrder
- [ ] `requestId` không blank
- [ ] Idempotency check trước khi tạo
- [ ] Dùng `customerId` thay `customerName`
- [ ] Batch load products + warehouses (tránh N+1)
- [ ] `buildOutboundOrder()` in-memory, không query DB
- [ ] `orderCode = "OUT-" + UUID` — không có space
- [ ] `@Transactional REQUIRES_NEW` chỉ bao `save()`
- [ ] Catch `DataIntegrityViolationException` cho race condition

### completeOrder
- [ ] So sánh `order.getStatus() != OrderStatus.PENDING` (enum, không phải string)
- [ ] `reserveAllItems` rollback items trước nếu fail giữa chừng
- [ ] `rollbackReservedItems` xử lý cả hot (Redis) và cold (DB)
- [ ] `commitOrder()` là `public` (không phải `private`)
- [ ] Optimistic lock với `updateOrderStatus(PENDING → COMPLETED)`
- [ ] Outbox event cho TẤT CẢ items (hot + cold)
- [ ] Reload order sau commit để `mapToResponse` trả status đúng
- [ ] `rollbackReservedItems` không throw trong loop — log ERROR và tiếp tục

### Outbox Pattern
- [ ] `outbox_events` có `idx_outbox_pending` index
- [ ] Dispatcher dùng `fixedDelay` (không phải `fixedRate`)
- [ ] Retry tối đa 5 lần rồi chuyển `DEAD`
- [ ] Cleanup SENT events hàng ngày (cron 3AM)
- [ ] `@EnableScheduling` trong `WmsApplication`
- [ ] `spring.jpa.open-in-view=false`

### Kafka Consumer
- [ ] Idempotency key = `orderCode` + `productId`
- [ ] `inventory_log` có `UNIQUE (order_code, product_id)`
- [ ] `removeStock` + `INSERT inventory_log` trong cùng `@Transactional`
- [ ] Catch `DataIntegrityViolationException` — không re-throw
- [ ] Chỉ xử lý `OUTBOUND_HOT` — cold đã trừ DB ở Phase 1

### DB Pool
- [ ] `maximum-pool-size` = (core × 2) + 1
- [ ] `leak-detection-threshold=5000` — phát hiện transaction giữ connection quá lâu
- [ ] `open-in-view=false`
- [ ] Không có `@Transactional` bao quá rộng (validate + Redis + DB cùng lúc)
## Phase 10 - Warehouse-Level Permission & Low Stock Alert

### 10.1 Muc tieu

Phase 10 bo sung co che phan quyen chi tiet theo kho (warehouse-level ACL) de dam bao:
- Role chi quy dinh quyen global.
- Quyen thao tac thuc te duoc check theo tung kho.
- Check quyen tai service layer truoc khi vao nghiep vu/transaction.

### 10.2 Thiet ke quyen chi tiet (khuyen nghi production)

- Giu Role global: `ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_VIEWER`.
- Them bang permission master:
- `auth_permission(id, code, description)`.
- Code goi y: `INBOUND_CREATE`, `INBOUND_COMPLETE`, `OUTBOUND_CREATE`, `OUTBOUND_COMPLETE`, `INVENTORY_VIEW`, `INVENTORY_ADJUST`.
- Them bang mapping user-quyen-theo-kho:
- `auth_user_warehouse_permission(id, user_id, warehouse_id, permission_id, granted_by, granted_at)`.
- Rang buoc bat buoc:
- `UNIQUE(user_id, warehouse_id, permission_id)`.

### 10.3 Ma tran quyen goi y

- `ROLE_ADMIN`: bypass warehouse permission check (toan he thong).
- `ROLE_STAFF`: bat buoc co permission theo warehouse cho cac thao tac ghi.
- `ROLE_VIEWER`: chi duoc `INVENTORY_VIEW` theo warehouse.

Ap dung theo nghiep vu:
- Inbound `create/complete`: check warehouse cua tung item.
- Outbound `create/complete`: check warehouse cua tung item.
- Inventory transfer: check ca `fromWarehouseId` va `toWarehouseId`.

### 10.4 DTO/API quan tri quyen

DTO:
- `GrantWarehousePermissionRequest(userId, warehouseId, permissionCode)`.
- `RevokeWarehousePermissionRequest(userId, warehouseId, permissionCode)`.
- `WarehousePermissionResponse(userId, warehouseId, permissionCode, grantedAt, grantedBy)`.

API:
- `POST /api/v1/admin/warehouse-permissions/grant`
- `POST /api/v1/admin/warehouse-permissions/revoke`
- `GET /api/v1/admin/users/{userId}/warehouse-permissions`

### 10.5 Diem chen check quyen (service layer)

Tao `WarehouseAccessService`:
- `assertHasWarehousePermission(userId, warehouseId, permissionCode)`.
- `assertHasWarehousePermissionForItems(userId, List<Long> warehouseIds, permissionCode)`.

Chen vao dau cac ham nghiep vu:
- `InboundService.createOrder(...)`
- `InboundService.completeOrder(...)`
- `OutboundService.createOrder(...)`
- `OutboundCompleteService.completeOrder(...)`
- `InventoryService.transferStock(...)`

```mermaid
sequenceDiagram
    participant C as Controller
    participant S as Domain Service
    participant A as WarehouseAccessService
    participant R as PermissionRepo
    C->>S: request
    S->>A: assertHasWarehousePermission(userId, warehouseId, action)
    A->>R: existsByUserAndWarehouseAndPermission(...)
    R-->>A: true/false
    A-->>S: allow / throw AccessDeniedException
    S-->>C: continue business
```

### 10.6 Thu tu trien khai an toan

1. Tao entity + repository + unique index.
2. Seed permission master data (`auth_permission`).
3. Tao `WarehouseAccessService` + `AccessDeniedException`.
4. Gan check vao service layer (khong chi controller).
5. Them admin API `grant/revoke/list`.
6. Viet test unit (access service) va integration (inbound/outbound bi chan dung permission).

### 10.7 Low Stock Alert (phan con lai cua Phase 10)

- Them `min_stock_threshold` tai `inv_inventory` hoac `prd_product`.
- Tao job dinh ky quet `quantity < threshold`.
- Ghi canh bao vao bang `inv_alert`.
- Optional: publish Kafka event / notify.
- API:
- `GET /api/v1/inventory/alerts?warehouseId=...`
