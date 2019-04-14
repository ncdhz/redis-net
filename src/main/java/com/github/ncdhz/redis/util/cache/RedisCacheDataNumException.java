package com.github.ncdhz.redis.util.cache;

/**
 * redis 二级缓存的个数 如果个数解析错误会抛出
 */
public class RedisCacheDataNumException extends RuntimeException {
    public RedisCacheDataNumException(String message) {
        super(message);
    }
}
