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

### ðŸš€ Giai Ä‘oáº¡n hiá»‡n táº¡i Ä‘ang lÃ m

- [x] **Phase 6: TÃ­ch há»£p Redis Caching (Truy xuáº¥t siÃªu tá»‘c)**
    - [x] `1.` Má»Ÿ file `WmsApplication.java` vÃ báº­t `@EnableCaching`.
    - [x] `2.` Táº¡o file cáº¥u hÃ¬nh `RedisConfig.java` Ä‘á»ƒ Ã©p dá»¯ liá»‡u cache thÃ nh Ä‘á»‹nh dáº¡ng JSON chuáº©n.
    - [x] `3.` Cáº­p nháº­t `ProductService`: Gáº¯n `@Cacheable` cho hÃ m Get vÃ`@CacheEvict` cho hÃ m
      Create/Update/Delete.
    - [x] `4.` Cáº­p nháº­t `WarehouseService`: Gáº¯n Annotation tÆ°Æ¡ng tá»± cho Kho hÃ ng.

- [/] **Phase 7: NÃ¢ng cáº¥p Scalability & Data Integrity (Production-grade)**
    - [x] `1.` Cáº¥u hÃ¬nh `@TransactionalEventListener(phase = AFTER_COMMIT)` cho viá»‡c ghi Log.
    - [x] `2.` Táº¡o `StockMovementEvent`, `TransferStockEvent` vÃ Publisher trong `InventoryService`.
    - [x] `3.` Triá»ƒn khai Async Listener (`MovementTypeEvent`) lÆ°u Log sau khi Transaction Commit thÃ nh cÃ´ng.
    - [x] `4.` Denormalization Audit Log: LÆ°u `productName`, `warehouseName`, `userName` trá»±c tiáº¿p vÃ o báº£ng log.
    - [x] `5.` Táº¡o `InventoryLog` Entity vá»›i DB Constraint `UNIQUE(order_code, product_id)` â€” Final Gate chá»‘ng
      trÃ¹ng dá»¯
      liá»‡u.
    - [x] `6.` Atomic Update (`updateOrderStatus`): JPQL `UPDATE ... WHERE status = :oldStatus` cho Inbound & Outbound.
    - [x] `7.` Idempotency cho Create Order: `requestId` + `UNIQUE constraint` trÃªn `OutboundOrder`.
    - [x] `8.` Outbox Pattern: Táº¡o module `infrastructure/outbox` lÆ°u event trÆ°á»›c khi gá»­i Kafka.
    - [x] `9.` Redis Lua Script: Atomic decrease/increase stock + `processedKey` chá»‘ng trÃ¹ng lá»‡nh cho Hot Item.
    - [x] `10.` 2-Phase Reserve + Commit (`OutboundCompleteService`): Reserve ngoÃ i TX â†’ Commit trong TX ngáº¯n.
    - [x] `11.` Compensating Transaction: `rollbackReservedItems()` hoÃ n tráº£ cáº£ Redis láº«n DB khi fail giá»¯a
      chá»«ng.


- [x] **Phase 8: Quáº£n lÃ½ tráº¡ng thÃ¡i & Há»§y phiáº¿u (Order Life-cycle)** ðŸš€
    - [x] ThÃªm tráº¡ng thÃ¡i `CANCELLED` vÃ o `OrderStatus`.
    - [x] Triá»ƒn khai hÃ m `cancelOrder` cho Inbound (PENDING -> CANCELLED).
    - [x] Triá»ƒn khai hÃ m `cancelOrder` cho Outbound (PENDING -> CANCELLED).

[//]: # (- [ ] **Phase 9: Quáº£n lÃ½ Váº­n chuyá»ƒn &#40;Shipping & Carrier&#41;** ðŸš€)

[//]: # (    - [ ] Táº¡o module `Carrier` quáº£n lÃ½ Ä‘Æ¡n vá»‹ váº­n chuyá»ƒn &#40;GHTK, Viettel Post, v.v.&#41;.)

[//]: # (    - [ ] Gáº¯n thÃ´ng tin váº­n chuyá»ƒn vÃ o phiáº¿u Xuáº¥t kho &#40;`trackingNumber`, `shippingFee`&#41;.)

- [x] **Phase 10: Há»‡ thá»‘ng Cáº£nh bÃ¡o & PhÃ¢n quyá»n nÃ¢ng cao**

[//]: # (    - [ ] Low Stock Alert &#40;Cáº£nh bÃ¡o hÃ ng dÆ°á»›i má»©c tá»‘i thiá»ƒu&#41;.)

    - [x] PhÃ¢n quyá»n chi tiáº¿t theo tá»«ng Kho (Warehouse-level RBAC).

- [x] **Phase 11: BÃ¡o cÃ¡o & Thá»‘ng kÃª (Reports & Dashboard)**
    - [x] HoÃ n thiá»‡n `InventoryReportService`.
    - [x] API Dashboard tá»•ng há»£p.

- [ ] **Phase 12: Test Stability & Release Gate**
    - [ ] Cháº¡y pass `mvn test` toÃ n bá»™ project.

[//]: # (    - [ ] Bá»• sung integration test cho luá»“ng Inbound/Outbound/Transfer.)

[//]: # (    - [ ] Bá»• sung test permission theo kho &#40;allow/deny theo role + warehouse&#41;.)

[//]: # (    - [ ] Bá»• sung test Outbox Dispatcher + retry/dead flow.)

- [ ] **Phase 13: Database Migration & Data Safety**
    - [ ] TÃ­ch há»£p `Flyway` hoáº·c `Liquibase`.
    - [ ] Táº¯t `ddl-auto=update` á»Ÿ mÃ´i trÆ°á»ng production.
    - [ ] Chuáº©n hÃ³a index/unique constraint cho báº£ng lá»›n (`inv_`, `outbox_`, `auth_` mapping).
    - [ ] Thiáº¿t káº¿ vÃ kiá»ƒm thá»­ backup/restore Ä‘á»‹nh ká»³.

- [ ] **Phase 14: Security Hardening**
    - [/] Đưa JWT secret/config nhạy cảm ra env/secret manager. (Code/config đã sẵn sàng, runtime secret manager cho
      prod: pending)
    - [x] Thiáº¿t láº­p chÃ­nh sÃ¡ch JWT TTL + rotation.
    - [x] ThÃªm rate limit cho endpoint auth/login.
    - [ ] Audit log cho thao tÃ¡c admin (grant/revoke permission).

- [ ] **Phase 15: Event Reliability & Idempotency**
    - [ ] GiÃ¡m sÃ¡t vÃ xá»­ lÃ½ `outbox DEAD` (manual replay/re-drive).
    - [ ] RÃ soÃ¡t idempotency end-to-end cho consumer.
    - [ ] Chuáº©n hÃ³a retry/backoff cho producer/consumer Kafka.
    - [ ] Viáº¿t runbook xá»­ lÃ½ lá»‡ch tá»“n kho do event fail.

- [ ] **Phase 16: Observability & Monitoring**
    - [ ] Metrics á»©ng dá»¥ng: latency, error rate, throughput.
    - [ ] Metrics háº¡ táº§ng: DB pool, Kafka lag, Redis hit ratio.
    - [ ] Structured logging + `traceId/correlationId`.
    - [ ] Alerting cho low stock, outbox dead, consumer failure, DB saturation.

- [ ] **Phase 17: Deployment & Go-Live Readiness**
    - [ ] TÃ¡ch profile `dev/staging/prod` vÃ externalize config.
    - [ ] CI/CD pipeline: build, test, scan, migrate, smoke test.
    - [ ] Chuáº©n hÃ³a chiáº¿n lÆ°á»£c rollback release.
    - [ ] HoÃ n thiá»‡n runbook váº­n hÃ nh vÃ incident response.
