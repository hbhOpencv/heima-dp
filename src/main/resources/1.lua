-->俩个参数,第一个为优惠券id,第二个为用户id
local voucherId = ARGV[1];
local userId = ARGV[2];
-->优惠券库存key
local voucherKey = "seckill:stock:" .. voucherId;
-->订单key
local orderKey = "seckill:order:" .. voucherId;
-->判断优惠券库存是否足够
if tonumber(redis.call('get', voucherKey)) <= 0 then
    return 1;
end
-->判断用户是否已购买,已购买则返回2
if redis.call('sismember',orderKey,userId) == 1 then
    return 2;
end
redis.call('incr',voucherKey,-1);
redis.call('sadd',orderKey,userId);
return 0;
