package com.github.ncdhz.redis.net;

/**
 * 连接错误
 * @author majunlong
 */
public class RedisNetConnectException extends RuntimeException {

    public RedisNetConnectException(String err) {
        super(err);
    }
}
