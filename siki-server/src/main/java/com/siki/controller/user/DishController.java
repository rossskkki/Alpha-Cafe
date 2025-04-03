package com.siki.controller.user;

import com.siki.constant.StatusConstant;
import com.siki.entity.Dish;
import com.siki.result.Result;
import com.siki.service.DishService;
import com.siki.utils.CacheClient;
import com.siki.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.siki.constant.RedisConstants.*;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheClient cacheClient;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
//        //构造redis的key
//        String key = "dish_" + categoryId;
//
//        //查询redis中是否存在菜品数据
//        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
//        //如果存在，直接返回
//        if (list != null) {
//            return Result.success(list);
//        }
//        //如果不存在，查询数据库，然后存入redis
//        Dish dish = new Dish();
//        dish.setCategoryId(categoryId);
//        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

//        list = dishService.listWithFlavor(dish);
//        redisTemplate.opsForValue().set(key, list);

        //使用缓存击穿解决方案
//        List<DishVO> list = cacheClient.queryWithLogicalExpire(CACHE_DISH_KEY, categoryId, DishVO.class, this::listWithFlavor, CACHE_DISH_TTL, java.util.concurrent.TimeUnit.SECONDS, LOCK_DISH_KEY);
        //使用缓存穿透解决方案
        List<DishVO> list = cacheClient.queryWithPassThrough(CACHE_DISH_KEY, categoryId, List.class, this::listWithFlavor, CACHE_DISH_TTL, TimeUnit.MINUTES);
        //使用缓存雪崩解决方案


        return Result.success(list);
    }

    public List<DishVO> listWithFlavor(Long categoryId) {
        //查询数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        return dishService.listWithFlavor(dish);
    }
}
