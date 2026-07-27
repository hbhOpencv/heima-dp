package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class HmDianPingApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IShopService shopService;
    //shopService.saveShop2Redis(1L, LocalDateTime.now().plusMinutes(1));//bean的方法只能在class
//    @Test
//    void test1() throws InterruptedException {
//        shopService.saveShop2Redis(1L, 2L);
//    }
    @Resource
    private RedisIdWorker redisIdWorker;
    @Test
    void test2() throws InterruptedException {
        //编写代码测试nextId(String keyPrefix)方法
        //开一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(300);
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        Runnable task = () -> {
            for (int i = 0; i < 200; i++) {
                Long id = redisIdWorker.nextId("order");
                System.out.println(id);
            }
            countDownLatch.countDown();
        };
        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }
        countDownLatch.await();
    }

    @Test
    void test3() throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(2);
        list.add(1);
        for(int i = 0,j = list.size()-1;i<=j;i++,j--){
            if (list.get(i) == list.get(j)) {
                System.out.println("是回文");
            }
        }

    }
    @Test
    void test5(){
        List<Shop> shopList = shopService.list();
        Map<Long, List<Shop>> map = shopList.stream().collect(Collectors.groupingBy(Shop::getTypeId));//stream流的用法？
        for(Map.Entry<Long, List<Shop>> entry : map.entrySet()){
            Long id = entry.getKey();
            String key = SHOP_GEO_KEY + id;
            List<Shop> shops = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(shops.size());//?
            for (Shop shop : shops) {
                // //将当前type的商铺都添加到locations集合中
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),new Point(shop.getX(),shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }

    }





    }





