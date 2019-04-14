package com.github.ncdhz.redis.util.cache;

import com.github.ncdhz.redis.handler.RedisCommand;
import com.github.ncdhz.redis.net.RedisConf;
import com.github.ncdhz.redis.net.RedisDatabaseConf;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author majunlong
 */
public class RedisNetCache implements RedisCache {

    private final RedisCacheHandler cacheHandler;
    /**
     * redis data 的第一级缓存
     * 此缓存中的数据还没来得及处理完成
     */
    private Map<String,RedisData> redisCache = new ConcurrentHashMap<>();

    private RedisConf conf;

    private volatile boolean close = false;

    private RedisDatabaseConf databaseConf;

    private ThreadPoolExecutor poolExecutor;

    public RedisNetCache(RedisConf conf, ThreadPoolExecutor poolExecutor, RedisCacheHandler cacheHandler){
        this.conf = conf;
        this.databaseConf = (RedisDatabaseConf) conf;
        this.poolExecutor = poolExecutor;
        this.cacheHandler = cacheHandler;
        initRedisCache();
    }


    @Override
    public void putData(String key, RedisData redisData) {
        redisCache.put(key,redisData);
    }

    @Override
    public void putData(RedisCommand command,String key, String value) {
        RedisData redisData = getRedisData(command, value);
        redisCache.put(key,redisData);
    }

    @Override
    public String getData(String key){
        RedisData value = redisCache.get(key);
        return value==null?null:value.getData().toString();
    }

    @Override
    public RedisData getRedisData(RedisCommand command, String value){
        return new RedisNetData(value,command,databaseConf.getAllHostAndPort());
    }

    @Override
    public void close(){
        redisCache.clear();
        close = true;
    }


    private void initRedisCache(){
        poolExecutor.execute(()-> {
                String redisCacheTimeStr = conf.getProperty("redis.cache.time");
                if (redisCacheTimeStr==null||"".equals(redisCacheTimeStr)) {
                    redisCacheTimeStr = "1000";
                    conf.set("redis.cache.time",redisCacheTimeStr);
                }
                Integer redisCacheTime = null;
                try {
                    redisCacheTime = Integer.valueOf(redisCacheTimeStr);
                }catch (Exception e){
                    try {
                        poolExecutor.shutdown();
                        throw new RedisCacheTimeException("[redis.cache.time="+redisCacheTimeStr+"] Non-standard configuration");
                    }catch (RedisCacheTimeException r){
                        r.printStackTrace();
                        System.exit(0);
                    }
                }
                while (!close){
                    for (String key : redisCache.keySet()) {
                        RedisData redisData = redisCache.get(key);
                        if(cacheHandler.cacheHandler(key, redisData)){
                            redisCache.remove(key);
                        }
                    }
                    try {
                        Thread.sleep(redisCacheTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
    }


}
