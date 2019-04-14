package com.github.ncdhz.redis.net;

import java.util.List;

/**
 * @author majunlong
 */
public interface RedisDatabaseConf extends Iterable<List<RedisDatabase>>{

    /**
     * 获取所有的redisDatabase的配置
     * @return 返回所有的redisDatabase的配置
     */
    List<List<RedisDatabase>> getAllRedisDatabase();

    /**
     * 设置redisDatabase配置
     * @param redisDatabase 一个数据库的配置
     */
    void setRedisDatabase(RedisDatabase redisDatabase);

    /**
     * 设置redisDatabase配置
     * @param redisDatabases database配置的集合
     */
    void setRedisDatabase(List<RedisDatabase> redisDatabases);

    /**
     * 根据host和ip获取redisDatabase
     * @param host 主机地址
     * @param port 主机端口
     * @return 一个redisDatabase的配置
     */
    RedisDatabase getRedisDatabase(String host, Integer port);

    /**
     * 获取所有的主机地址和端口
     * @return 所有的主机地址和端口
     */
    List<List<String[]>> getAllHostAndPort();

    /**
     * 获取一个 redisDatabase 的类
     * @return 返回一个redisDatabase 的类
     */
    RedisDatabase getRedisDatabase();

    /**
     * 设置一个redisDatabase的配置到总配置文件
     * @param host 主机地址
     * @param port 主机端口
     */
    void setRedisDatabase(String host, Integer port);

    Boolean isInit();

    void setInit(boolean init);
}
