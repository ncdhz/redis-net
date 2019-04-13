package com.github.ncdhz.redis.cache;


import com.github.ncdhz.redis.net.RedisNetConf;

/**
 * 用于单独操作某一组redis数据库
 * @author majunlong
 */
public class RedisUtils implements Redis {



    public RedisUtils(RedisNetConf conf) {
        dataCache=new  RedisDataCache(conf);
    }

    private DataCache dataCache;

    /**
     * 通过字符串到数据库查找数据
     * @param key 数据库存储数据用的的key
     * @return 返回找到的数据
     */
    @Override
    public String get(String key) {
        //在redisData 缓存中找数据 找到数据直接返回没有找到获取 Jedis 操作数据库

        return (String) dataCache.get(key);
    }

    @Override
    public Object set(String key,Object value){
        return dataCache.set(key,value,RedisCommand.SET);
    }

    @Override
    public void close() {
        dataCache.getRedisPool().close();
    }
}
