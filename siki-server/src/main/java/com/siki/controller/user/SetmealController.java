package com.siki.controller.user;

import com.siki.constant.StatusConstant;
import com.siki.entity.Setmeal;
import com.siki.result.Result;
import com.siki.service.SetmealService;
import com.siki.utils.CacheClient;
import com.siki.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.siki.constant.RedisConstants.CACHE_SETMEAL_KEY;
import static com.siki.constant.RedisConstants.CACHE_SETMEAL_TTL;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    private static final Logger log = LoggerFactory.getLogger(SetmealController.class);
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheClient cacheClient;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("开始查询分类{}的套餐", categoryId);

        //使用缓存穿透解决方案
        List<Setmeal> list = cacheClient.queryWithPassThrough(CACHE_SETMEAL_KEY, categoryId, List.class, this::listWithFlavor, CACHE_SETMEAL_TTL, TimeUnit.MINUTES);
        return Result.success(list);
    }


    public List<Setmeal> listWithFlavor(Long categoryId) {
        //查询数据库
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);
        List<Setmeal> list = setmealService.list(setmeal);
        return list;
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }
}
