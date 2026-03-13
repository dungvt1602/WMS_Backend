# WMS Project Implementation Checklist

- [ ] **Phase 1: Core Infrastructure & Shared Setup**
    - [ ] Configure Database Connection (PostgreSQL properties).
    - [ ] Setup `BaseEntity` with JPA Auditing (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`).
    - [ ] Setup Standard API Response wrapper (e.g., `ApiResponse<T>`).
    - [ ] Setup Global Exception Handler (`@RestControllerAdvice`).
    - [ ] Setup Flyway or Hibernate Auto-DDL for database schema generation.

- [ ] **Phase 2: Authentication & Authorization (Security)**
    - [ ] Create `User` and `Role` entities in Auth module.
    - [ ] Implement Spring Security Configuration (`SecurityFilterChain`, CORS, AuthEntryPoint).
    - [ ] Implement JWT Service (Generate, Validate tokens).
    - [ ] Implement JwtAuthenticationFilter.
    - [ ] Implement `AuthService` (Login, Register).
    - [ ] Create `AuthController`.

- [ ] **Phase 3: Master Data Modules**
    - [ ] Product Module (CRUD).
    - [ ] Warehouse Module (CRUD).

- [ ] **Phase 4: Core Engine Modules**
    - [ ] Inventory Module (Manage Stock, Optimistic Locking `version`).
    
- [ ] **Phase 5: Input/Output Modules**
    - [ ] Inbound Module (Nhập kho).
    - [ ] Outbound Module (Xuất kho).
