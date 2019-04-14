package com.github.ncdhz.redis.util.cache;

public interface RedisCacheHandler {

    /**
     * 缓存处理程序
     * @param key 缓存数据的key
     * @param redisData 缓存数据
     * @return 处理是否成功
     */
    boolean cacheHandler(String key, RedisData redisData);
}
