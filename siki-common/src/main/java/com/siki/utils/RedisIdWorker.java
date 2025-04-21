package com.siki.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1640995200L; // 2000-01-01 00:00:00
    private static final int COUNT_BITS = 32; // 序列号占用的位数

    //注入redis
    private RedisTemplate redisTemplate;

    public RedisIdWorker(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long nextId(String keyPrefix){
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        //生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = redisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        //拼接并返回
        timestamp = timestamp << COUNT_BITS;
        return timestamp | count;
    }
}
