package com.github.ncdhz.redis.net;

import com.github.ncdhz.redis.handler.*;
import com.github.ncdhz.redis.util.cache.RedisCache;
import com.github.ncdhz.redis.util.cache.RedisNetCache;
import com.github.ncdhz.redis.util.pool.RedisNetPool;
import com.github.ncdhz.redis.util.pool.RedisPool;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Random;

/**
 * RedisNet默认实现类
 * @author majunlong
 */
public class RedisNetContext implements RedisContext{

    private final RedisConf conf;

    private RedisBinary redisBinary;

    private Redis redis;

    private RedisClient redisClient;

    public static synchronized RedisContext getRedisNet(RedisNetConf conf){
        return new RedisNetContext(conf);
    }
    private static Random random = new Random();

    private RedisThreadPool threadPool;
    /**
     * 初始化RedisNet
     * @param conf redis-net 的配置类
     */
    private RedisNetContext(RedisConf conf){
        this.conf = conf;

        RedisDatabaseConf databaseConf = (RedisDatabaseConf) conf;
        if (!databaseConf.isInit()){
            RedisInit redisInit= new RedisNetInit(conf);
            redisInit.initNetUrl().initDatabase().initPassword();
            databaseConf.setInit(true);
        }
        threadPool= new RedisThreadPool(10);

        RedisPool redisPool = new RedisNetPool(conf,threadPool);

        RedisCache redisCache = new RedisNetCache(conf,threadPool,(key, redisData)->{
            List<List<String[]>> hostAndUrls = redisData.getHostAndUrl();
            for (List<String[]> hostAndUrl : hostAndUrls) {
                String[] hostUrl = hostAndUrl.get(random.nextInt(hostAndUrl.size()));
                Jedis redis = redisPool.getRedis(hostUrl[0].trim(), Integer.valueOf(hostUrl[1].trim()));
                if (redis!=null) {
                    redis.set(key,redisData.getData().toString());
                    redis.close();
                    hostAndUrls.remove(hostAndUrl);
                }
            }
            return hostAndUrls.size()==0;
        });

        this.redis = new RedisNet(redisPool,redisCache);
        this.redisBinary = new RedisNetBinary(redisPool,redisCache);
        this.redisClient = new RedisNetClient(redisPool,redisCache);
    }


    @Override
    public String set(final String key, final String value) {
        return redis.set(key,value,"xx");
    }

    @Override
    public String set(final String key, String value, final String nxxx, final String expx, final long time) {
        return null;
    }

    @Override
    public String set(final String key, final String value, final String expx,final long time) {
        return null;
    }

    @Override
    public String set(final String key,final  String value,final String nxxx) {
        return redis.set(key,value,nxxx);
    }

    @Override
    public String set(final byte[] key, final byte[] value) {
        return redisBinary.set(key,value,"xx".getBytes());
    }

    @Override
    public String set(final byte[] key, final byte[] value, final byte[] nxxx) {
        return redisBinary.set(key,value,nxxx);
    }

    @Override
    public String set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final long time) {
        return null;
    }

    @Override
    public String set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final int time) {
        return null;
    }

    @Override
    public String get(final String key) {
        return redis.get(key);
    }

    @Override
    public byte[] get(final byte[] key) {
        return redisBinary.get(key);
    }

    @Override
    public Boolean exists(String key) {
        return redis.exists(key);
    }

    @Override
    public Long exists(String... keys) {
        return redis.exists(keys);
    }

    @Override
    public Boolean exists(byte[] key) {
        return redisBinary.exists(key);
    }

    @Override
    public Long exists(byte[]... keys) {
        return redisBinary.exists(keys);
    }

    @Override
    public void close() {
        try {
            Thread.sleep(Integer.valueOf(conf.getProperty("redis.cache.time")));
            threadPool.shutdown();
            redisClient.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
