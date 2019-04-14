package com.github.ncdhz.redis.handler;

/**
 * 连接错误
 * @author majunlong
 */
public class RedisNetConnectException extends RuntimeException {

    public RedisNetConnectException(String err) {
        super(err);
    }
}
