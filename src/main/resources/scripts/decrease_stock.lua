-- KEYS[1] = stock key
-- KEYS[2] = processed key (requestId)

-- ARGV[1] = quantity

local stockKey = KEYS[1]
local processedKey = KEYS[2]
local quantity = tonumber(ARGV[1])

-- 1. Check đã xử lý chưa (idempotent)
if redis.call('EXISTS', processedKey) == 1 then
    return 1 -- đã xử lý rồi → không trừ nữa
end

-- 2. Lấy tồn kho hiện tại
local current_stock = tonumber(redis.call('GET', stockKey) or "0")

-- 3. Check đủ hàng không
if current_stock < quantity then
    return -1 -- không đủ hàng
end

-- 4. Trừ kho
local new_stock = redis.call('DECRBY', stockKey, quantity)

-- 5. Mark request đã xử lý (TTL 1h)
redis.call('SET', processedKey, "1", "EX", 3600)

-- 6. Return stock mới
return new_stock