-- KEYS[1] = stock key
-- KEYS[2] = processed key (requestId)
 
-- ARGV[1] = quantity
-- ARGV[2] = maxStock (giới hạn tối đa của kho, 0 = không giới hạn)
 
local stockKey     = KEYS[1]
local processedKey = KEYS[2]
local quantity     = tonumber(ARGV[1])
local maxStock     = tonumber(ARGV[2] or "0")
 
-- 1. Check đã xử lý chưa (idempotent)
if redis.call('EXISTS', processedKey) == 1 then
    return 1 -- đã xử lý rồi → không cộng nữa
end
 
-- 2. Lấy tồn kho hiện tại
local current_stock = tonumber(redis.call('GET', stockKey) or "0")
 
-- 3. Check vượt maxStock không (nếu maxStock = 0 thì bỏ qua check)
if maxStock > 0 and (current_stock + quantity) > maxStock then
    return -1 -- vượt quá giới hạn kho
end
 
-- 4. Cộng kho
local new_stock = redis.call('INCRBY', stockKey, quantity)
 
-- 5. Mark request đã xử lý (TTL 1h)
redis.call('SET', processedKey, "1", "EX", 3600)
 
-- 6. Return stock mới
return new_stock
 