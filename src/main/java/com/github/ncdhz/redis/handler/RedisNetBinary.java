package com.github.ncdhz.redis.handler;
import com.github.ncdhz.redis.util.cache.RedisCache;
import com.github.ncdhz.redis.util.pool.RedisPool;

/**
 * @author majunlong
 */
public class RedisNetBinary implements RedisBinary {

    private Redis redis;

    public RedisNetBinary(RedisPool redisPool, RedisCache redisCache) {
        this.redis = new RedisNet(redisPool,redisCache);
    }

    @Override
    public byte[] get(byte[] key) {
        if (key==null){
            throw new RedisDataException("value sent to redis cannot be null");
        }
        return redis.get(new String(key)).getBytes();
    }

    @Override
    public String set(byte[] key, byte[] value, byte[] nxxx) {
        if (value==null||nxxx==null){
            throw new RedisDataException("value sent to redis cannot be null");
        }
        return redis.set(new String(key),new String(value),new String(nxxx));
    }

    @Override
    public Boolean exists(byte[] key) {
        return redis.exists(new String(key));
    }

    @Override
    public Long exists(byte[]... keys) {
        String[] keyss = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            keyss[i] = new String(keys[i]);
        }
        return redis.exists(keyss);
    }
}
