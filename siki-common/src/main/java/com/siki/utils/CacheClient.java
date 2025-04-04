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
        redisTemplate.opsForValue().set(key, redisData);
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

    public <R, ID> R queryWithLogicalExpire(String keyPrefix1, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit, String keyPrefix2) {
        String key = keyPrefix1 + id;
        // 从缓存中获取
        Object value = redisTemplate.opsForValue().get(key);
        // 如果缓存中有数据，直接返回
        if (value == null) {
            return null;
        }
        
        // 命中，判断获取的数据类型
        RedisData redisData;
        if (value instanceof RedisData) {
            // 如果已经是 RedisData 类型，直接使用
            redisData = (RedisData) value;
        } else if (value instanceof String) {
            // 如果是字符串，需要转换
            redisData = JSONUtil.toBean((String) value, RedisData.class);
        } else {
            // 其他情况，可能是序列化问题，记录日志并返回 null
            log.error("缓存数据类型错误: {}", value.getClass().getName());
            return null;
        }
        
        // 获取数据并转换为目标类型
        R r;
        if (redisData.getData() instanceof JSONObject) {
            r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        } else {
            try {
                r = (R) redisData.getData();
            } catch (ClassCastException e) {
                log.error("数据类型转换错误", e);
                return null;
            }
        }
        
        LocalDateTime expireTime = redisData.getExpireTime();
        // 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回
            return r;
        }
        
        // 过期，需要缓存重建
        //尝试获取锁
        String lockKey = keyPrefix2 + id;
        boolean isLock = tryLock(lockKey);
        //判断是否获取到锁
        if (isLock){
            //成功，开启独立线程重建缓存
            CACHE_REBUILD_POOL.submit(() -> {
                //查询店铺数据
                try {
                    R r1 = dbFallback.apply(id);
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
        return r;
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
