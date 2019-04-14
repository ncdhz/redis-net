package com.github.ncdhz.redis.handler;



import com.github.ncdhz.redis.util.cache.RedisCache;
import com.github.ncdhz.redis.util.pool.RedisPool;

/**
 * 用于单独操作某一组redis数据库
 * @author majunlong
 */
public class RedisNet implements Redis {


    private final RedisClient redisClient;

    private static final String NX = "nx";

    private static final String XX = "xx";

    public RedisNet(RedisPool redisPool, RedisCache redisCache) {
        this.redisClient = new RedisNetClient(redisPool,redisCache);
    }

    @Override
    public String get(String key) {
        if (key==null){
            throw new RedisDataException("value sent to redis cannot be null");
        }
        return redisClient.get(RedisCommand.GET,key);
    }

    @Override
    public String set(String key, String value, String nxxx) {
        if (value==null||nxxx==null){
            throw new RedisDataException("value sent to redis cannot be null");
        }
        if (NX.equals(nxxx)){
            return redisClient.set(RedisCommand.SETNX,key,value);
        }
        if (XX.equals(nxxx)){
            return redisClient.set(RedisCommand.SETXX,key,value);
        }
        throw new RedisDataException("ERR syntax error");
    }

    @Override
    public Long exists(String... keys) {
        return redisClient.exists(RedisCommand.EXISTS,keys);
    }

    @Override
    public Boolean exists(String key) {
        return redisClient.exists(RedisCommand.EXISTS,key)==1;
    }
}
