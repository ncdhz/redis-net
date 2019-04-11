package com.github.ncdhz.redis.net;

import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * redis-net 的配置类
 * @author majunlong
 */
public class RedisNetConf extends JedisPoolConfig {

    private Properties conf= System.getProperties();

    public RedisNetConf(){

        this.setTestWhileIdle(true);
        this.setMinEvictableIdleTimeMillis(60000L);
        this.setTimeBetweenEvictionRunsMillis(30000L);
        this.setNumTestsPerEvictionRun(-1);
    }

    public RedisNetConf(String key,String value){
        this();
        conf.put(key,value);
    }

    public RedisNetConf(Properties conf){
        System.setProperties(conf);
    }

    /**
     * 设置参数
     * @param key 参数名
     * @param value 参数值
     * @return 返回配置对象
     */
    public RedisNetConf set(String key,String value){
        conf.put(key,value);
        return this;
    }

    /**
     * 获取RedisNet
     */
    public RedisNet getRedisNet(){
        return new RedisNetContext(this);
    }

}
