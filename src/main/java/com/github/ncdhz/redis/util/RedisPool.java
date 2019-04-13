package com.github.ncdhz.redis.util;

import redis.clients.jedis.Jedis;

public interface RedisPool {

    void close();


    RedisThreadPool getRedisThreadPool();

    boolean isClose();

    Jedis getRedis(String host, Integer port);
}
