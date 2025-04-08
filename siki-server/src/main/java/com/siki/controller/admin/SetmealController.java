package com.siki.controller.admin;

import com.siki.dto.SetmealDTO;
import com.siki.dto.SetmealPageQueryDTO;
import com.siki.entity.Setmeal;
import com.siki.result.PageResult;
import com.siki.result.Result;
import com.siki.service.SetmealService;
import com.siki.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.siki.constant.RedisConstants.CACHE_SETMEAL_KEY;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐接口")
public class SetmealController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
//    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);
        // 删除套餐缓存
        clearCache(CACHE_SETMEAL_KEY + "*");
        return Result.success();
    }

    /**
     * 分页查询套餐
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除套餐")
//    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除套餐：{}", ids);
        setmealService.deleteBatch(ids);
        // 删除套餐缓存
        clearCache(CACHE_SETMEAL_KEY + "*");
        return Result.success();
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getByIdWithDish(@PathVariable Long id) {
        log.info("更新套餐：{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 更新套餐状态
     * @param id, status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("更新套餐状态")
//    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result updateStatus(@RequestParam Long id, @PathVariable Integer status) {
        log.info("更新套餐状态：{}", id);
        setmealService.updateStatus(id, status);
        // 删除套餐缓存
        clearCache(CACHE_SETMEAL_KEY + "*");
        return Result.success();
    }

    /**
     * 更新套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("更新套餐")
//    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("更新套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        // 删除套餐缓存
        clearCache(CACHE_SETMEAL_KEY + "*");
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
