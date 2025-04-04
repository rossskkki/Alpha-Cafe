package com.siki.sikiserver;

import com.siki.entity.Dish;
import com.siki.service.DishService;
import com.siki.utils.CacheClient;
import com.siki.vo.DishVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


import java.util.concurrent.TimeUnit;

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

    @Test
    void testSaveShop() throws InterruptedException {
        DishVO dish = dishService.getByIdWithFlavor(75L);
        cacheClient.setWithLogicalExpire(CACHE_DISH_KEY+75L, dish, 30L, TimeUnit.MINUTES);
    }
}
