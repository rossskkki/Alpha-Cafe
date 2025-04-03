package com.siki.utils;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {
    private RedisTemplate redisTemplate;

    private static final ExecutorService CACHE_REBUILD_POOL = Executors.newFixedThreadPool(10);

    public CacheClient(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        //设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        redisData.setData(value);
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 从缓存中获取
        Object value = redisTemplate.opsForValue().get(key);
        // 如果缓存中有数据，直接返回
        if (value != null && !"".equals(value)) {
            return (R) value;
        }
        //如果缓存中有空值，直接返回null
        if (value != null) {
            return null;
        }

        // 如果缓存中没有数据，从数据库中获取
        R r = dbFallback.apply(id);
        // 如果数据库中有数据，写入缓存
        if (r != null) {
            this.set(key, r, time, unit);
            return r;
        }else{
            //空值写入缓存
            redisTemplate.opsForValue().set(key, "", time, unit);
            return null;
        }
    }

    public <R, ID> List<R>  queryWithLogicalExpire(String keyPrefix1, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long time, TimeUnit unit, String keyPrefix2) {
        String key = keyPrefix1 + id;
        // 从缓存中获取
        String json = redisTemplate.opsForValue().get(key).toString();
        // 如果缓存中有数据，直接返回
        if (StrUtil.isBlank(json)) {
            return null;
        }
        //命中，需要先把json转为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回
            return (List<R>) r;
        }
        //过期，需要缓存重建
        //尝试获取锁
        String lockKey = keyPrefix2 + id;
        boolean isLock = tryLock(lockKey);
        //判断是否获取到锁
        if (isLock){
            //成功，开启独立线程重建缓存
            CACHE_REBUILD_POOL.submit(() -> {
                //查询店铺数据
                try {
                    List<R> r1 = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unlock(lockKey);
                }
            });
        }
        //返回过期的商品信息
        return (List<R>) r;
    }

    // 获取锁
    private boolean tryLock(String key) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    // 释放锁
    private void unlock(String key) {
        redisTemplate.delete(key);
    }
}
