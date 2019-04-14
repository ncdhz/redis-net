package com.github.ncdhz.redis.handler;

public interface RedisBinary {

    /**
     * 从redis中获取数据
     * @param key 数据的key
     * @return 返回指定key的数据
     */
    byte[] get(byte[] key);

    /**
     * 插入数据到 redis
     * @param key 数据的key
     * @param value 数据的value
     * @return 返回是否成功
     */
    String set(byte[] key, byte[] value,byte[] nxxx);


    Boolean exists(byte[] key);

    Long exists(byte[] ... keys);
}
