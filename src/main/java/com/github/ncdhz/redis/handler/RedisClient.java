package com.github.ncdhz.redis.handler;

public interface RedisClient {
    /**
     * 关闭数据库的所有连接
     */
    void close();
    /**
     * 添加数据到数据库
     */
    String set(RedisCommand command,String key,String value);
    /**
     * 充数据库获取数据
     */
    String get(RedisCommand command,String key);

    Long exists(RedisCommand command,String ... keys);
}
