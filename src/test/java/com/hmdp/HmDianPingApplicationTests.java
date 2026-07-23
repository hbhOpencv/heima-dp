package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@SpringBootTest
class HmDianPingApplicationTests {
    @Autowired
    private ShopServiceImpl shopService;
    //shopService.saveShop2Redis(1L, LocalDateTime.now().plusMinutes(1));//bean的方法只能在class
    @Test
    void test1() throws InterruptedException {
        shopService.saveShop2Redis(1L, 2L);
    }
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


    }





