package com.github.ncdhz.redis.util;

import com.github.ncdhz.redis.cache.RedisCommand;
import redis.clients.jedis.Jedis;
/**
 * 用于单独操作某一组redis数据库
 */
public class RedisUtils {

    /**
     * 通过字符串到数据库查找数据
     * @param key 数据库存储数据用的的key
     * @return 返回找到的数据
     */
    public static String get(String key) {
        //在redisData 缓存中找数据 找到数据直接返回没有找到获取 Jedis 操作数据库
        Object value = RedisCommand.GET.getData(key);
        try {
            if (value==null){
                Jedis jedis = RedisPoolUtils.getRedis();
                value = jedis.get(key);
                jedis.close();
            }
        }catch (Exception e){
            return get(key);
        }
        return (String) value;
    }

    public static Object set(String key,Object value){
        return RedisCommand.SET.addData(key,value);
    }
}
