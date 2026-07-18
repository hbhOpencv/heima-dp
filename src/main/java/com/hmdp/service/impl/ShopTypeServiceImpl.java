package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result queryList() {
        List<String> typeList = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_LIST_KEY, 0, -1);
        //判断缓存是否为空
        if(!typeList.isEmpty()){
            List<ShopType> list = new ArrayList<>();
            for (String s : typeList) {
                ShopType bean = JSONUtil.toBean(s, ShopType.class);
                list.add(bean);
            }
            return Result.ok(list);
        }
        //缓存为空，查询数据库
        //数据库查询
        List<ShopType> sort = query().orderByAsc("sort").list();
        //数据库中也没有数据
        if(sort == null) return Result.fail("查询失败");
        //数据库中有数据，缓存到Redis
        for (ShopType shopType : sort) {
            String jsonStr = JSONUtil.toJsonStr(shopType);
            stringRedisTemplate.opsForList().leftPush(CACHE_SHOP_TYPE_LIST_KEY, jsonStr);
        }
        return Result.ok(sort);


    }
}
