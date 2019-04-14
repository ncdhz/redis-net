package com.github.ncdhz.redis.util.cache;


import com.github.ncdhz.redis.handler.RedisCommand;

/**
 * 数据缓存的接口
 * @author majunlong
 */
public interface RedisCache {


    void putData(String key, RedisData redisData);

    void putData(RedisCommand command, String key, String value);

    String getData(String key);

    RedisData getRedisData(RedisCommand command, String value);

    void close();
}
