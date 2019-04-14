package com.github.ncdhz.redis.util.pool;

/**
 * RedisPool 里面没有可以能够连接 redis 数据库的连接器时抛出
 */
public class RedisPoolNullException extends RuntimeException {
    public RedisPoolNullException(String message) {
        super(message);
    }
}
