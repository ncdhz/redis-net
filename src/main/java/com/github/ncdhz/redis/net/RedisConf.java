package com.github.ncdhz.redis.net;

public interface RedisConf{


    /**
     * 设置配置到配置文件
     * @param key 配置的名称
     * @param value 配置的值
     * @return 返回
     */
    RedisConf set(String key, String value);

    /**
     * 获取配置值的对象
     * @param key 配置的名称
     * @return 配置的值
     */
    Object get(String key);

    /**
     * 获取配置值的字符串
     * @param key 配置的名称
     * @return 配置的值
     */
    String getProperty(String key);

}
