package com.github.ncdhz.redis.net;

import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;
/**
 * redis-net 的配置类
 * @author majunlong
 */
public class RedisNetConf extends JedisPoolConfig {

    private Properties conf= new Properties();
    /**
     * 初始化配置
     */
    public RedisNetConf(){
        super();
    }

    public RedisNetConf(String key,String value){
        super();
        conf.put(key,value);
    }

    public RedisNetConf(Properties conf){
        super();
        this.conf = conf;
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

    public Object get(String key) {
        return conf.get(key);
    }

    public void put(String key, Object value) {
        conf.put(key,value);
    }

    public String getProperty(String key) {
        return conf.getProperty(key);
    }

    public void setProperty(String key, String value) {
        conf.setProperty(key,value);
    }
}
