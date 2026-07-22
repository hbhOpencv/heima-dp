package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
//Redis分布式锁的初版
public class SimpleRedisLock implements ILock{

    private static final String KEY_PREFIX = "lock:";
    private StringRedisTemplate stringRedisTemplate;
    private String name;

    public SimpleRedisLock(StringRedisTemplate stringRedisTemplate,String name){
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    @Override
    public boolean tryLock(long time) {//是秒级时间
        long id = Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, "thread"+id, time, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(success);
    }

    @Override
    public void deleteLock() {
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }
}
