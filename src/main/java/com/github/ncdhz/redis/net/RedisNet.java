package com.github.ncdhz.redis.net;

/**
 * redis的操作接口
 * @author majunlong
 */
public interface RedisNet{
    /**
     * 添加数据到redis中
     * @param key 数据的name
     * @param value 数据的具体类容
     * @return
     */
    String set(String key,String value);

    /**
     * 通过key获取value
     */
    String get(String key);

    /**
     * 关闭所有redis连接
     */
    void close();
}
