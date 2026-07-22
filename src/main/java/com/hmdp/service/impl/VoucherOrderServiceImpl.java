package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import lombok.NonNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        //判断秒杀是否过期
        LocalDateTime endTime = seckillVoucher.getEndTime();
        if(LocalDateTime.now().isAfter(endTime)){
            return Result.fail("优惠券已过期");
        }
        //判断库存是否充足
        Integer stock = seckillVoucher.getStock();
        if(stock<1){
            return Result.fail("优惠券已售罄");
        }
        Long userId = UserHolder.getUser().getId();
        SimpleRedisLock simpleRedisLock = new SimpleRedisLock(stringRedisTemplate, "Order" + userId);
        if(!simpleRedisLock.tryLock(1200)){
            return Result.fail("每个用户只能下一单");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            simpleRedisLock.deleteLock();
        }

    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        /*判断用户是否已购买过该优惠券，一个用户只能买一种秒杀优惠券的一个*/
        Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if(count>0){
            return Result.fail("您已购买过该优惠券");
        }
        //扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).gt("stock",0)//判断库存是否充足（乐观锁）
                // .eq("stock",stock)判断此时库存是否和刚刚读的一样，不一样则说明有其他用户秒杀了，无法执行，这个失败率太高不如使用上面这个
                .update();//这是个啥操作?更新秒杀优惠券的库存
        if(!success){//为啥在这判断库存是否充足？
            //因为秒杀是并发操作,可能有多个用户同时秒杀,导致库存不足
            return Result.fail("库存不足");
        }
        Long orderId = redisIdWorker.nextId("voucher_order");//生成订单id
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setCreateTime(LocalDateTime.now());
        voucherOrder.setUserId(userId);//设置下单的用户id，
        // 拦截器中获取用户id，userHolder.getUser().getId()
        //保存订单
        save(voucherOrder);
        //返回订单id
        return Result.ok(orderId);
    }
}
