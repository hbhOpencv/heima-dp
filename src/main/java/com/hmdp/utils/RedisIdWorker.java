package com.hmdp.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.print.attribute.standard.MediaSize;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    private static final long BEGIN_TIMESTAMP = 1672491599L;
    private static final long COUNT_BITS = 32L;
    @Autowired
    private  StringRedisTemplate stringRedisTemplate;
    //构造方法注入
//    @Autowired
//    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }


    public  Long nextId(String key){
        //时间戳的计算
        LocalDateTime now = LocalDateTime.now();
        long epochSecond = now.toEpochSecond(ZoneOffset.UTC);
        long time = epochSecond - BEGIN_TIMESTAMP;
        //序列号的计算
        //获取当前的日期

        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long increment = stringRedisTemplate.opsForValue().increment("inc" +":" + key + ":" + date);

        //拼接时间戳和序列号,左移32位,再或操作
        return time<<COUNT_BITS | increment;
    }

//
//    public static void main(String[] args) {
//        LocalDateTime localDateTime = LocalDateTime.of(2022, 12, 31, 12, 59, 59);
//        long epochSecond = localDateTime.toEpochSecond(ZoneOffset.UTC);
//        System.out.println(epochSecond);
//    }


}
