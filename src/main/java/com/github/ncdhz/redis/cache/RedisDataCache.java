package com.github.ncdhz.redis.cache;

import com.github.ncdhz.redis.net.RedisNetConf;
import com.github.ncdhz.redis.util.RedisPool;
import com.github.ncdhz.redis.util.RedisPoolUtils;
import com.github.ncdhz.redis.util.RedisThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author majunlong
 */
public class RedisDataCache implements DataCache{


    private static Logger logger = LoggerFactory.getLogger(RedisDataCache.class);
    /**
     * redis data 的第一级缓存
     * 此缓存中的数据还没来得及处理完成
     */
    private static Map<String,RedisData> redisDataCache1 = new ConcurrentHashMap<>();
    /**
     * redis data 的第二级缓存
     * 此缓存中的数据是已经处理完成的
     */
    private static LinkedHashMap<String,Object> redisDataCache2 = new LinkedHashMap<>();

    private RedisNetConf conf;


    private static Random random = new Random();

    private Integer redisCacheDataNumber;

    private RedisPool redisPool;

    private RedisThreadPool redisThreadPool;

    RedisDataCache(RedisNetConf conf){
        this.conf = conf;
        redisPool = new RedisPoolUtils(conf);
        redisThreadPool = redisPool.getRedisThreadPool();
        initCacheDataNumber();
        initRedisCache();
    }

    public static Map<String,RedisData> getRedisDataCache1() {
        return redisDataCache1;
    }

    public static LinkedHashMap<String, Object> getRedisDataCache2() {
        return redisDataCache2;
    }


    private void initCacheDataNumber(){

        String redisCacheDataNumberSys = conf.getProperty("redis.cache.data.number");
        if (redisCacheDataNumberSys==null||"".equals(redisCacheDataNumberSys)){
            redisCacheDataNumberSys = "5000";
            conf.setProperty("redis.cache.data.number",redisCacheDataNumberSys);
        }
        try {
            redisCacheDataNumber = Integer.valueOf(redisCacheDataNumberSys);
        }catch (Exception e){
            logger.error("[redis.cache.data.number={}] Non-standard configuration",redisCacheDataNumberSys);
            try {
                throw new RedisCacheDataNumException("[redis.cache.data.number="+redisCacheDataNumberSys+"] Non-standard configuration");
            }catch (RedisCacheDataNumException r){
                r.printStackTrace();
                System.exit(0);
            }
        }
    }

    private void initRedisCache(){
        redisThreadPool.execute(()-> {
                String redisCacheTimeStr = conf.getProperty("redis.cache.time");
                if (redisCacheTimeStr==null||"".equals(redisCacheTimeStr)) {
                    redisCacheTimeStr = "1000";
                    conf.setProperty("redis.cache.time",redisCacheTimeStr);
                }
                Integer redisCacheTime = null;
                try {
                    redisCacheTime = Integer.valueOf(redisCacheTimeStr);
                }catch (Exception e){
                    logger.error("[redis.cache.time={}] Non-standard configuration",redisCacheTimeStr);
                    try {
                        throw new RedisCacheTimeException("[redis.cache.time="+redisCacheTimeStr+"] Non-standard configuration");
                    }catch (RedisCacheTimeException r){
                        r.printStackTrace();
                        System.exit(0);
                    }
                }
                while (!redisPool.isClose()){
                    for (String key : redisDataCache1.keySet()) {
                        RedisData redisData = redisDataCache1.get(key);
                        if (cacheProcessing(key, redisData)){
                            setRedisDataCache2(key,redisData.getData());
                            redisDataCache1.remove(key);
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

    /**
     * 添加元素到第二级缓存如果 缓存满了删除最后一个缓存元素
     */
    private void setRedisDataCache2(String key,Object value) {
        redisDataCache2.put(key,value);
        if(redisDataCache2.size()==redisCacheDataNumber){
            Set<String> keys = redisDataCache2.keySet();
            String[] k = keys.toArray(new String[0]);
            redisDataCache2.remove(k[k.length-1]);
        }
    }




    @Override
    public Object get(String key) {
        Object value = redisDataCache2.remove(key);
        if (value==null){
            RedisData redisData = redisDataCache1.get(key);
            if (redisData!=null){
                value = redisData.getData();
            }
        }else {
            setRedisDataCache2(key,value);
        }
        return value;
    }

    @Override
    public Object set(String key, Object value,RedisCommand command) {
        List<List<String[]>> hostAndUrl = conf.getAllHostAndPort();
        RedisData redisData = new RedisData(value,command,hostAndUrl);
        redisDataCache1.put(key,redisData);
        return value;
    }


    /**
     * 用于处理缓存数据
     */
    private boolean cacheProcessing(String key, RedisData redisData) {
        List<List<String[]>> hostAndUrls = redisData.getHostAndUrl();
        for (List<String[]> hostAndUrl : hostAndUrls) {
            String[] hostUrl = hostAndUrl.get(random.nextInt(hostAndUrl.size()));
            Jedis redis = redisPool.getRedis(hostUrl[0].trim(), Integer.valueOf(hostUrl[1].trim()));
            if (redis!=null) {
                redis.set(key, (String) redisData.getData());
                hostAndUrls.remove(hostAndUrl);
                redis.close();
            }
        }
        return hostAndUrls.size()==0;
    }



    @Override
    public RedisPool getRedisPool() {
        return redisPool;
    }

    class RedisData{

        private Object data;

        private RedisCommand command;

        private List<List<String[]>> hostAndUrl;

        RedisData(Object data, RedisCommand command, List<List<String[]>> hostAndUrl) {
            this.data = data;
            this.command = command;
            this.hostAndUrl = new CopyOnWriteArrayList<>(hostAndUrl);
        }

        Object getData() {
            return data;
        }


        List<List<String[]>> getHostAndUrl() {
            return hostAndUrl;
        }

    }
}
