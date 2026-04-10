# WMS Project Implementation Checklist

- [x] **Phase 1: Core Infrastructure & Shared Setup**
    - [x] Setup `BaseEntity` with JPA Auditing.
    - [x] Configure Database Connection.
    - [x] Setup Standard API Response wrapper.
    - [x] Setup Global Exception Handler.
    - [ ] Setup Flyway or Hibernate Auto-DDL.

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

- [x] **Phase 5: Inbound & Outbound (Business Flow)**
    - [x] Create common `OrderStatus` enum.
    - [x] **Inbound Module** ✅
    - [x] **Outbound Module** ✅
        - [x] `OutboundOrder` and `OutboundOrderItem` entities.
        - [x] DTOs.
        - [x] `OutboundService`.
        - [x] `OutboundController`.
