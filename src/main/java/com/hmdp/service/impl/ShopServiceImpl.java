package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.coyote.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {
        //redis查询商铺缓存
        String s = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //判断缓冲是否命中
        if(StrUtil.isBlank(s)) {//缓存为空
            Shop shop = getById(id);
            //数据库中查询商铺是否存在
            //不存在
            if(shop == null)  {
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("商铺不存在");
            }
            //存在则保存到redis中
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return Result.ok(shop);
        }
        if("".equals(s)) return Result.fail("商铺不存在");//缓存中为空字符串
        //返回商铺信息
        Shop shop = JSONUtil.toBean(s, Shop.class);
        return Result.ok(shop);
    }

    @Transactional
    //开启事物
    //更新商铺信息
    @Override
    public Result updateByBody(Shop shop) {
        if(shop.getId() == null) return Result.fail("商铺id不能为空");
        updateById(shop);
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();


    }

}

