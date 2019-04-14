package com.github.ncdhz.redis.util.pool;

import redis.clients.jedis.Jedis;


public interface RedisPool {

    /**
     * 关闭线程池
     */
    void close();


    /**
     * 获取Jedis
     * @param host 主机地址
     * @param port 主机端口
     * @return 返回一个Jedis
     */
    Jedis getRedis(String host, Integer port);

    /**
     * 获取Redis
     * @return 返回redis
     */
    Jedis getRedis();
}
