# WMS Project Implementation Checklist

- [x] **Phase 1: Core Infrastructure & Shared Setup**
    - [x] Setup `BaseEntity` with JPA Auditing (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`).
    - [x] Configure Database Connection (PostgreSQL properties).
    - [x] Setup Standard API Response wrapper (e.g., `ApiResponse<T>`).
    - [x] Setup Global Exception Handler (`@RestControllerAdvice`).
    - [ ] Setup Flyway or Hibernate Auto-DDL for database schema generation.

- [ ] **Phase 2: Authentication & Authorization (Security)**
    - [x] Create `User` and `Role` entities in Auth module.
    - [x] Create `UserRepository` and `RoleRepository`.
    - [x] Add JWT Dependencies (JJWT) to `pom.xml`.
    - [x] Implement Spring Security Configuration (`SecurityFilterChain`, CORS, AuthEntryPoint).
    - [x] Implement JWT Service (Generate, Validate tokens).
    - [ ] Implement JwtAuthenticationFilter.
    - [ ] Implement `AuthService` (Login, Register).
    - [ ] Create `AuthController`.

- [ ] **Phase 3: Master Data Modules**
    - [ ] Product Module (CRUD).
    - [ ] Warehouse Module (CRUD).

- [ ] **Phase 4: Core Core Modules**
    - [ ] Inventory Module (Manage Stock, Optimistic Locking `version`).
    
- [ ] **Phase 5: Input/Output Modules**
    - [ ] Inbound Module (Nhập kho).
    - [ ] Outbound Module (Xuất kho).
