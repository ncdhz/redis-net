package com.github.ncdhz.redis.cache;

import com.github.ncdhz.redis.util.RedisPool;

/**
 * 数据缓存的接口
 * @author majunlong
 */
public interface DataCache {

    /**
     * 通过key获取数据
     * @param key key
     * @return 数据库查询的数据
     */
    Object get(String key);

    /**
     * 添加数据到数据库
     * @param key 数据的key
     * @param value 数据的值
     * @param command 数据的操作符
     * @return 返回添加数据的值
     */
    Object set(String key,Object value,RedisCommand command);
    /**
     * 获取 RedisPool
     */
    RedisPool getRedisPool();
}
