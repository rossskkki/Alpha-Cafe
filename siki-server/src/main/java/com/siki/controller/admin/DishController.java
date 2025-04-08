package com.siki.controller.admin;

import com.siki.dto.DishDTO;
import com.siki.dto.DishPageQueryDTO;
import com.siki.entity.Dish;
import com.siki.result.PageResult;
import com.siki.result.Result;
import com.siki.service.DishService;
import com.siki.utils.CacheClient;
import com.siki.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.siki.constant.RedisConstants.*;

/**
 * 菜品控制器
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheClient cacheClient;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 删除缓存
        String key = CACHE_DISH_KEY + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        // 删除缓存
        clearCache(CACHE_DISH_KEY+"*");
        clearCache(CACHE_DISHDETAIL_KEY+"*");
        return Result.success();
    }

    /**
     * 删除热点菜品
     * @param ids
     * @return
     */
    @DeleteMapping("/hotdish")
    @ApiOperation("删除热点菜品")
    public Result deleteHotDish(@RequestParam List<Long> ids) {
        log.info("删除热点菜品：{}", ids);
        dishService.deleteBatch(ids);
        // 删除缓存
        clearCache(CACHE_DISH_KEY+"*");
        clearCache(CACHE_DISHDETAIL_KEY+"*");
        clearCache(CACHE_HOTDISH_KEY+"*");
        for (Long id : ids) {
            clearCache(CACHE_HOTDISHDETAIL_KEY+id);
        }
        //获取现有的热点菜品
        List<DishVO> hotDishList = dishService.hotDishDisplay();
        // 设置热点菜品的缓存
        log.info("缓存预热：{} {}", CACHE_HOTDISH_KEY+0, hotDishList);
        cacheClient.setWithLogicalExpire(CACHE_HOTDISH_KEY+0, hotDishList, 10L, TimeUnit.SECONDS);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 删除缓存
        clearCache(CACHE_DISH_KEY+"*");
        clearCache(CACHE_DISHDETAIL_KEY+dishDTO.getId());
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(@RequestParam Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> dishList = dishService.getByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 修改菜品状态
     * @param id
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public Result updateStatus(@RequestParam Long id, @PathVariable Integer status) {
        log.info("修改菜品状态：id={}, status={}", id, status);
        dishService.updateStatus(id, status);
        // 删除缓存
        clearCache(CACHE_DISH_KEY+"*");
        clearCache(CACHE_DISHDETAIL_KEY+id);
        return Result.success();
    }

    /**
     * 新增热点菜品
     * @return
     */
    @PostMapping("/hotdish")
    @ApiOperation("新增热点菜品")
    public Result addHotdish(@RequestBody DishDTO dishDTO) {
        log.info("新增热点菜品：{}", dishDTO);
        // 新增热点菜品
        DishVO dish = dishService.saveHotWithFlavor(dishDTO);
        //获取现有的热点菜品
        List<DishVO> hotDishList = dishService.hotDishDisplay();
        // 删除缓存
        String key = CACHE_DISH_KEY + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        log.info("缓存预热：{}", CACHE_HOTDISHDETAIL_KEY+dish.getId());
        // 缓存预热
        cacheClient.setWithLogicalExpire(CACHE_HOTDISHDETAIL_KEY+dish.getId(), dish, 10L, TimeUnit.SECONDS);
        // 设置热点菜品的缓存
        log.info("缓存预热：{}", CACHE_HOTDISH_KEY+0);
        cacheClient.setWithLogicalExpire(CACHE_HOTDISH_KEY+0, hotDishList, 10L, TimeUnit.SECONDS);
        return Result.success();
    }

    /**
     * 清除缓存
     * @param pattern
     */
    private void clearCache(String pattern) {
        log.info("清除缓存：{}", pattern);
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
        while (cursor.hasNext()) {
            String key =new String(cursor.next());
            log.info("删除缓存：{}", key);
            redisTemplate.delete(key);
        }
    }
}



