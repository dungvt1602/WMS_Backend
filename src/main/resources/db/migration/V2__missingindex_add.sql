---------------------Tao index cho inv_inventory---------
-- Tim ton kho theo san pham hoac theo kho
CREATE INDEX idx_inventory_warehouse_product ON inv_inventory (product_id, warehouse_id);

-- Tim inventory theo zone
CREATE INDEX idx_inventory_zone ON inv_inventory (zone_id)

-- Tìm theo status (AVAILABLE, RESERVED...)
CREATE INDEX idx_inv_inventory_status
    ON inv_inventory(status);

---------------------Tao index cho inv_stockmovenment-------------
CREATE INDEX  idx_inv_stock_movement_warehouseid ON  inv_stock_movements(warehouse_id, created_at DESC )

-- Lọc theo sản phẩm
CREATE INDEX idx_stock_movements_product
    ON inv_stock_movements(product_id);

-- Lọc theo reference_code (tra cứu đơn hàng)
CREATE INDEX idx_stock_movements_reference
    ON inv_stock_movements(reference_code);



---------------------Tao index cho auth_refresh_token-------------\
CREATE INDEX idx_refresh_token_hash
    ON auth_refresh_token(token_hash);

-- Tìm token theo user
CREATE INDEX idx_refresh_token_user
    ON auth_refresh_token(user_id, revoked);

---------------------Tao index cho inb_inbound_order-------------\
CREATE INDEX idx_inbound_order_customer
    ON inb_inbound_order(customer_id);

CREATE INDEX idx_inbound_order_status_created
    ON inb_inbound_order(status, created_at DESC);


---------------------out_outbound_order---------------------------
CREATE INDEX idx_outbound_order_customer
    ON out_outbound_order(customer_id);

CREATE INDEX idx_outbound_order_status_created
    ON out_outbound_order(status, created_at DESC);

---------------------inv_transfer_reports — bảng báo cáo, query lọc nhiều chiều:---------------------------

CREATE INDEX idx_transfer_reports_product
    ON inv_transfer_reports(product_id, created_at DESC);

CREATE INDEX idx_transfer_reports_warehouse
    ON inv_transfer_reports(from_warehouse_id, created_at DESC);

---------------------auth_user — login query theo username/email::---------------------------

CREATE INDEX idx_auth_user_username
    ON auth_user(username);

CREATE INDEX idx_auth_user_email
    ON auth_user(email);