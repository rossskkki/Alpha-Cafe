package com.siki.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_DISH_TTL = 30L;
    public static final Long CACHE_SETMEAL_TTL = 30L;
    public static final String CACHE_DISH_KEY = "cache:dish:";
    public static final String CACHE_HOTDISH_KEY = "cache:hotdish:";
    public static final String CACHE_DISHDETAIL_KEY = "cache:dishdetail:";
    public static final String CACHE_HOTDISHDETAIL_KEY = "cache:hotdishdetail:";
    public static final String CACHE_SETMEAL_KEY = "cache:setmeal:";
    public static final String CACHE_SETMEALDETAIL_KEY = "cache:setmealdetail:";
    public static final String PICKUP_CODE_KEY = "pickup_code:";

    public static final String CACHE_SHOP_TYPE_KEY = "cache:type:";
    public static final Long CACHE_SHOP_TYPE_TTL = 30L;

    public static final String LOCK_DISH_KEY = "lock:dish:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
