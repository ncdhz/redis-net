package com.github.ncdhz.redis.cache;

/**
 * redisCache 缓存刷入数据库的时间
 * @author majunlong
 */
public class RedisCacheTimeException extends RuntimeException {
    public RedisCacheTimeException(String message) {
        super(message);
    }
}
