package com.github.ncdhz.redis.handler;

/**
 * redis 数据出现错误抛出
 */
public class RedisDataException extends RuntimeException {
    public RedisDataException(String message) {
        super(message);
    }
}
