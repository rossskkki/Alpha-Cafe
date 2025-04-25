package com.siki.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class SimpleRedisLock implements Lock{
    private String name;
    private RedisTemplate redisTemplate;

    public SimpleRedisLock(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    private static final String LOCK_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static{
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String threadId = ID_PREFIX+Thread.currentThread().getId();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void unlock() {
        //调用lua脚本
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(LOCK_PREFIX + name), ID_PREFIX+Thread.currentThread().getId());
    }

//    @Override
//    public void unlock() {
//        //获取当前线程的标识
//        String threadId = ID_PREFIX+Thread.currentThread().getId();
//        //获取锁中标识
//        String id = redisTemplate.opsForValue().get(LOCK_PREFIX + name).toString();
//        //判断是否是当前线程
//        if (threadId.equals(id)) {
//            //是当前线程，删除锁
//            redisTemplate.delete(LOCK_PREFIX + name);
//        }
//    }
}
