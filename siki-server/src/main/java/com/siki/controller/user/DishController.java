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
import org.springframework.web.bind.annotation.*;

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
        log.info("开始查询分类{}的菜品", categoryId);
//        long startTime = System.currentTimeMillis();
        
        // 使用缓存穿透解决方案
        List<DishVO> list = cacheClient.queryWithPassThrough(CACHE_DISH_KEY, categoryId, List.class, this::listWithFlavor, CACHE_DISH_TTL, TimeUnit.MINUTES);
        
//        long endTime = System.currentTimeMillis();
//        log.info("查询分类{}的菜品完成，耗时{}ms", categoryId, (endTime - startTime));
        
        return Result.success(list);
    }


    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> dishDisplay(@PathVariable Long id) {
        log.info("开始查询菜品id为{}的菜品", id);
        //查询数据库
        DishVO dish = cacheClient.queryWithLogicalExpire(CACHE_DISH_KEY, id, DishVO.class, this::getByIdWithFlavor, CACHE_DISH_TTL, TimeUnit.MINUTES, LOCK_DISH_KEY);
        return Result.success(dish);
    }


    public List<DishVO> listWithFlavor(Long categoryId) {
        //查询数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        return dishService.listWithFlavor(dish);
    }

    public DishVO getByIdWithFlavor(Long id) {
        //查询数据库
        return dishService.getByIdWithFlavor(id);
    }

}
