package com.github.ncdhz.redis.util.pool;

/**
 * 当redisPool自动扫描时间不是数字时抛出
 */
public class RedisPoolTimeException extends RuntimeException {
    public RedisPoolTimeException(String message) {
        super(message);
    }
}
