package com.siki.utils;

public interface Lock {
    /**
     * 尝试获取锁
     * @param timeoutSec
     * @return true表示获取锁成功，false表示获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
