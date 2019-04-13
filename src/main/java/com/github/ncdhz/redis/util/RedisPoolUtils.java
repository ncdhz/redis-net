package com.github.ncdhz.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author majunlong
 */
public class RedisPoolUtils {

    private static Logger logger = LoggerFactory.getLogger(RedisPoolUtils.class);
    /**
     * 存放未失效的JedisPool
     */
    private static Map<Integer,List<RedisPool>> goodsRedisPool = new ConcurrentHashMap<>();
    /**
     * 存放失效的JedisPool
     */
    private static List<RedisPool> badRedisPool = new CopyOnWriteArrayList<>();
    /**
     * 用于随机获取一个 Jedis 的随机数生成器
     */
    private static Random randomRedisPoolNum = new Random();

    private static volatile boolean close = false;

    /**
     * 用于计数JedisPool 的编号
     */
    private static int count = 1;

    private static RedisThreadPool redisThreadPool = RedisThreadPool.getRedisThreadPool();


    static {
        checkBadPool();
        checkGoodPool();
    }
    /**
     * 用于检查好的连接池里面的数据是否已经挂掉
     */
    private static void checkGoodPool() {
        redisThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String redisPoolTimeSys = System.getProperty("redis.good.pool.time");
                if (redisPoolTimeSys==null||"".equals(redisPoolTimeSys)){
                    redisPoolTimeSys = "1000";
                    System.setProperty("redis.good.pool.time",redisPoolTimeSys);
                }
                Integer redisPoolTime = null;
                try {
                    redisPoolTime = Integer.valueOf(redisPoolTimeSys);
                }catch (Exception e){
                    logger.error("[redis.good.pool.time={}] Non-standard configuration",redisPoolTimeSys);
                    try {
                        throw new RedisPoolTimeException("[redis.good.pool.time="+redisPoolTimeSys+"] Non-standard configuration");
                    }catch (RedisPoolTimeException e1){
                        e1.printStackTrace();
                        System.exit(0);
                    }
                }

                while (!RedisPoolUtils.isClose()) {
                    for (List<RedisPool> value : goodsRedisPool.values()) {
                        for (RedisPool redisPool : value) {
                            getRedis(value,redisPool);
                        }
                    }
                    if (goodsRedisPool.size()==0){
                        Pool2Zero();
                    }
                    try {
                        Thread.sleep(redisPoolTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 用于检查坏的连接池里面的数据是否已经恢复
     */
    private static void checkBadPool(){
        redisThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String redisPoolTimeSys = System.getProperty("redis.bad.pool.time");
                if (redisPoolTimeSys==null||"".equals(redisPoolTimeSys)){
                    redisPoolTimeSys = "1000";
                    System.setProperty("redis.bad.pool.time",redisPoolTimeSys);
                }
                Integer redisPoolTime = null;
                try {
                    redisPoolTime = Integer.valueOf(redisPoolTimeSys);
                }catch (Exception e){
                    logger.error("[redis.bad.pool.time={}] Non-standard configuration",redisPoolTimeSys);
                    try {
                        throw new RedisPoolTimeException("[redis.bad.pool.time="+redisPoolTimeSys+"] Non-standard configuration");
                    }catch (RedisPoolTimeException e1){
                        e1.printStackTrace();
                        System.exit(0);
                    }
                }
                while (!RedisPoolUtils.isClose()) {
                    for (RedisPool redisPool : badRedisPool) {
                        if (redisPool.isActivity()){
                            set(redisPool,redisPool.getNumber());
                        }
                    }
                    try {
                        Thread.sleep(redisPoolTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 从RedisPool中获取Jedis
     * @return 一个Jedis
     */
    public static Jedis getRedis(){
        if (goodsRedisPool.size()==0){
            Pool2Zero();
        }
        Set<Integer> countAll = goodsRedisPool.keySet();

        int allNum = randomRedisPoolNum.nextInt(countAll.size());
        Integer count = countAll.toArray(new Integer[0])[allNum];
        List<RedisPool> redisPools = goodsRedisPool.get(count);
        while (redisPools.size()!=0){
            int redisNum = randomRedisPoolNum.nextInt(redisPools.size());
            RedisPool redisPool = redisPools.get(redisNum);
            if (redisPool.isClosed()){
                redisPools.remove(redisPool);
            }else {
                Jedis redis = getRedis(redisPools, redisPool);
                if (redis!=null){
                    return redis;
                }
            }
        }
        goodsRedisPool.remove(count);
        return getRedis();
    }

    /**
     *  如果好的 pool 已经没有了
     *  就会调用此方法
     */
    private static void Pool2Zero(){
        try {
            close();
            logger.error("Redis connector empty，Check that your Redis database is open or that your configuration is correct");
            throw new RedisPoolNullException("Redis connector empty，Check that your Redis database is open or that your configuration is correct");
        }catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * 通过地址和端口获取redis
     * @param host 地址
     * @param port 端口
     * @return 一个 jedis 或者 null
     */
    public static Jedis getRedis(String host,Integer port){

        for (List<RedisPool> value : goodsRedisPool.values()) {
            for (RedisPool redisPool : value) {
                String host1 = redisPool.getHost();
                Integer port1 = redisPool.getPort();
                if (host.equals(host1)&&port.equals(port1)){
                    return getRedis(value,redisPool);
                }
            }
        }
        return null;
    }

    private static Jedis getRedis(List<RedisPool> redisPools,RedisPool redisPool){
        JedisPool jedisPool = redisPool.getJedisPool();
        try {
            Jedis resource = jedisPool.getResource();
            resource.ping();
            return resource;
        }catch (Exception e){
            redisPools.remove(redisPool);
            badRedisPool.add(redisPool);
        }
        return null;
    }

    /**
     * 用于关闭RedisPool
     */
    public static void close() {
        for (List<RedisPool> redisPools : goodsRedisPool.values()) {
            close(redisPools);
        }
        close(badRedisPool);
        setClose(true);
        RedisThreadPool.getRedisThreadPool().shutdown();
    }

    private static void close(Collection<RedisPool> redisPools){
        for (RedisPool redisPool : redisPools) {
            redisPool.close();
        }
    }
    /**
     * 构建redisPool 时使用
     * @param hostAndPorts host 和 port 的集合
     * @param redisNetConf redis 的配置参数
     */
    public static void set(List<String[]> hostAndPorts, JedisPoolConfig redisNetConf) {
        List<RedisPool> redisPools = new CopyOnWriteArrayList<>();
        for (String[]  hostAndPort: hostAndPorts) {
            String host = hostAndPort[0].trim();
            Integer port = Integer.valueOf(hostAndPort[1].trim());
            JedisPool jedisPool = new JedisPool(redisNetConf, host, port);
            RedisPool redisPool = new RedisPool(count, jedisPool, host, port);
            redisPools.add(redisPool);
        }
        goodsRedisPool.put(count++,redisPools);
    }

    private static void set(RedisPool redisPool,Integer number){
        List<RedisPool> redisPools = goodsRedisPool.get(number);
        if (redisPools==null){
            redisPools = new CopyOnWriteArrayList<>();
            redisPools.add(redisPool);
        }
        redisPools.add(redisPool);

        goodsRedisPool.put(number,redisPools);
    }



    public static boolean isClose() {
        return close;
    }

    public static void setClose(boolean close) {
        RedisPoolUtils.close = close;
    }


    static class RedisPool{
        /**
         * JedisPool 的编号
         */
        private int number;
        /**
         * 存放jedisPool
         */
        private JedisPool jedisPool;

        private String host;

        private Integer port;


        boolean isActivity(){
            try {
                Jedis resource = jedisPool.getResource();
                String ping = resource.ping();
                if (ping==null){
                    return false;
                }
                resource.close();
            }catch (Exception e){
                return false;
            }
            return true;
        }


        RedisPool(int number, JedisPool jedisPool, String host, Integer port) {
            this.number = number;
            this.jedisPool = jedisPool;
            this.host = host;
            this.port = port;
        }

        void close(){
            if (!jedisPool.isClosed()){
                jedisPool.close();
            }
        }

        boolean isClosed(){
            return jedisPool.isClosed();
        }


        int getNumber() {
            return number;
        }


        JedisPool getJedisPool() {
            return jedisPool;
        }


        String getHost() {
            return host;
        }

        Integer getPort() {
            return port;
        }
    }
}
