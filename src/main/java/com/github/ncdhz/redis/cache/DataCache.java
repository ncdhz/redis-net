package com.github.ncdhz.redis.cache;

/**
 * 数据缓存的接口
 * @author majunlong
 */
public interface DataCache {

    Object get(String key);

    Object set(String key,Object value,RedisCommand command);
}
