-- Index này giúp tăng tốc cực nhanh cho việc truy vấn lịch sử nhập xuất
-- theo từng sản phẩm cụ thể và sắp xếp theo thời gian mới nhất.
CREATE INDEX idx_stock_movement_product_created
    ON public.inv_stock_movements USING btree (product_id, created_at DESC);