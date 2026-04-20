local key = KEYS[1]
local quantity = tonumber(ARGV[1])
local current_stock = tonumber(redis.call('get', key) or "0")

if current_stock >= quantity then
    return redis.call('decrby', key, quantity)
else
    return -1
end