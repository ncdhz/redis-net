package com.github.ncdhz.redis.handler;

import com.github.ncdhz.redis.util.cache.RedisCache;
import com.github.ncdhz.redis.util.pool.RedisPool;
import redis.clients.jedis.Jedis;

/**
 * @author majunlong
 */
public class RedisNetClient implements RedisClient {


    private final RedisPool redisPool;

    private final RedisCache redisCache;

    private static final String SUCCESS = "OK";

    public RedisNetClient(RedisPool redisPool, RedisCache redisCache) {
        this.redisPool = redisPool;
        this.redisCache = redisCache;
    }

    @Override
    public void close() {
        redisCache.close();
        redisPool.close();
    }

    @Override
    public String set(RedisCommand command, String key, String value) {
        if (command.equals(RedisCommand.SETNX)){
            String value1 = get(command, key);
            if (value1!=null){
                return null;
            }
        }
        redisCache.putData(command,key,value);
        return SUCCESS;
    }

    @Override
    public String get(RedisCommand command, String key) {
        String data = redisCache.getData(key);
        if (data==null){
            Jedis redis = redisPool.getRedis();
            data = redis.get(key);
            redis.close();
        }
        return data;
    }

    @Override
    public Long exists(RedisCommand command, String... keys) {
        Jedis redis = redisPool.getRedis();
        return redis.exists(keys);
    }

}
