---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by 12419.
--- DateTime: 2025/5/4 下午9:32
---

--1. 参数列表
--1.1 优惠券id
--local test = 123
--redis.log(redis.LOG_NOTICE, "I CAN WRITE:" .. test)
local voucherId = ARGV[1]
redis.log(redis.LOG_NOTICE, "voucherId: " .. voucherId)
--1.2 用户id
local userId = ARGV[2]
redis.log(redis.LOG_NOTICE, "userId: " .. userId)
--1.3 订单id
local orderId = ARGV[3]

--2. Redis的key
--2.1 秒杀商品库存key
local stockKey = "seckill:stock:" .. voucherId
--2.2 订单key
local orderKey = "seckill:order:" .. voucherId

redis.log(redis.LOG_NOTICE, "Stock key: " .. stockKey)

--3. 判断库存是否充足
local stock = redis.call('get', stockKey)
redis.log(redis.LOG_NOTICE, stock)

if tonumber(stock) <= 0 then
    --3.1 库存不足，返回错误
    redis.log(redis.LOG_NOTICE, "Stock is nil for key: " .. stockKey)
    return 1
end

--4. 判断用户是否已经下单
local isOrder = redis.call("sismember", orderKey, userId)
if isOrder == 1 then
    --3.1 已经下单，返回错误
    return 2
end

--5. 扣减库存
redis.call("incrby", stockKey, -1)
--6. 下单成功，记录订单
redis.call("sadd", orderKey, userId)

--7. 发送详细到队列中(stream消息队列)
--redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)

--7. 返回成功
return 0