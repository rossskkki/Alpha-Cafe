package com.siki;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@MapperScan("com.siki.mapper") //扫描mapper接口
@Slf4j
@EnableCaching //开启缓存
@EnableScheduling //开启定时任务
@EnableAspectJAutoProxy(exposeProxy = true) //开启AOP
public class HkuAlphaCafeApplication {
    public static void main(String[] args) {
        SpringApplication.run(HkuAlphaCafeApplication.class, args);
        log.info("server started");
    }
}
