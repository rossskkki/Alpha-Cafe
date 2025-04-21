package com.siki.sikiserver;

import com.siki.entity.Dish;
import com.siki.service.DishService;
import com.siki.utils.CacheClient;
import com.siki.utils.RedisIdWorker;
import com.siki.vo.DishVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.siki.constant.RedisConstants.CACHE_DISHDETAIL_KEY;
import static com.siki.constant.RedisConstants.CACHE_DISH_KEY;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
class SikiServerApplicationTests {

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable tesk =() -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(tesk);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - begin));
    }

    @Test
    void testSaveShop() throws InterruptedException {
        DishVO dish = dishService.getByIdWithFlavor(75L);
        cacheClient.setWithLogicalExpire(CACHE_DISHDETAIL_KEY+75L, dish, 30L, TimeUnit.MINUTES);
    }
}
