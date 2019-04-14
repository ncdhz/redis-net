package com.github.ncdhz.redis.util;

import com.github.ncdhz.redis.net.RedisNetConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author majunlong
 */
public class RedisPoolUtils implements RedisPool {

    private static Logger logger = LoggerFactory.getLogger(RedisPoolUtils.class);

    private RedisNetConf conf;
    /**
     * 存放未失效的JedisPool
     */
    private Map<Integer, List<RedisPool>> goodsRedisPool = new ConcurrentHashMap<>();
    /**
     * 检测密码是否为空
     */
    private static final String PASSWORD_NULL = "null";
    /**
     * 存放失效的JedisPool
     */
    private List<RedisPool> badRedisPool = new CopyOnWriteArrayList<>();
    /**
     * 用于随机获取一个 Jedis 的随机数生成器
     */
    private static Random randomRedisPoolNum = new Random();

    /**
     * 当 close 为false时缓存池正常开启 当close为true时缓存池退出
     */
    private volatile boolean close = false;

    /**
     * 用于计数JedisPool 的编号
     */
    private int count = 1;

    private RedisThreadPool redisThreadPool = new RedisThreadPool(10);

    private Integer databaseTimeOut;

    public RedisPoolUtils(RedisNetConf conf) {
        this.conf = conf;
        databaseTimeOut = initDatabaseTimeOut(conf);
        List<List<RedisNetConf.RedisDatabase>> redisDatabases = conf.getAllRedisDatabase();
        for (List<RedisNetConf.RedisDatabase> redisDatabase : redisDatabases) {
            set(redisDatabase);
        }
        checkBadPool();
        checkGoodPool();
    }
    private Integer initDatabaseTimeOut(RedisNetConf conf){
        String outTimeStr = conf.getProperty("redis.database.time.out");
        if (outTimeStr==null||"".equals(outTimeStr)){
            outTimeStr = "2000";
        }
        try {
            return Integer.valueOf(outTimeStr);
        }catch (Exception e){
            initTimeErr("redis.database.time.out",outTimeStr);
        }
        return null;
    }
    /**
     * 构建redisPool 时使用
     *
     * @param redisDatabases redis database 一些基本配置
     */
    private void set(List<RedisNetConf.RedisDatabase> redisDatabases) {
        List<RedisPool> redisPools = new CopyOnWriteArrayList<>();
        for (RedisNetConf.RedisDatabase redisDatabase : redisDatabases) {
            Integer port = redisDatabase.getPort();
            String host = redisDatabase.getHost();
            String password = redisDatabase.getPassword();
            Integer database = redisDatabase.getDatabase();
            password = passwordIsNull(password)?null:password;
            database = database==null?0:database;

            JedisPool jedisPool =new JedisPool(conf,host,port,databaseTimeOut,password,database);
            RedisPool redisPool = new RedisPool(count, jedisPool, host, port);
            redisPools.add(redisPool);
        }
        goodsRedisPool.put(count++, redisPools);
    }

    private boolean passwordIsNull(String password){
        return password==null||"".equals(password)||password.toLowerCase().equals(PASSWORD_NULL);
    }


    /**
     * 获取线程池
     * @return 返回线程池
     */
    @Override
    public RedisThreadPool getRedisThreadPool() {
        return redisThreadPool;
    }

    /**
     * 用于检查好的连接池里面的数据是否已经挂掉
     */
    private void checkGoodPool() {
        redisThreadPool.execute(() -> {
            String redisPoolTimeStr = conf.getProperty("redis.good.pool.time");
            if (redisPoolTimeStr == null || "".equals(redisPoolTimeStr)) {
                redisPoolTimeStr = "1000";
                System.setProperty("redis.good.pool.time", redisPoolTimeStr);
            }
            Integer redisPoolTime = null;
            try {
                redisPoolTime = Integer.valueOf(redisPoolTimeStr);
            } catch (Exception e) {
                initTimeErr("redis.good.pool.time",redisPoolTimeStr);
            }

            while (!isClose()) {
                for (List<RedisPool> value : goodsRedisPool.values()) {
                    for (RedisPool redisPool : value) {
                        getRedis(value, redisPool);
                    }
                }
                if (goodsRedisPool.size() == 0) {
                    poolToZero();
                }
                try {
                    assert redisPoolTime != null;
                    Thread.sleep(redisPoolTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initTimeErr(String name,String value){
        try {
            logger.error("[{}={}] Non-standard configuration", name,value);
            throw new RedisPoolTimeException("["+name+"=" + value + "] Non-standard configuration");
        }catch (RedisPoolTimeException e){
            e.printStackTrace();
            System.exit(0);
        }
    }
    /**
     * 用于检查坏的连接池里面的数据是否已经恢复
     */
    private void checkBadPool() {
        redisThreadPool.execute(() -> {
            String redisPoolTimeStr = conf.getProperty("redis.bad.pool.time");
            if (redisPoolTimeStr == null || "".equals(redisPoolTimeStr)) {
                redisPoolTimeStr = "1000";
                System.setProperty("redis.bad.pool.time", redisPoolTimeStr);
            }
            Integer redisPoolTime = null;
            try {
                redisPoolTime = Integer.valueOf(redisPoolTimeStr);
            } catch (Exception e) {
                initTimeErr("redis.bad.pool.time",redisPoolTimeStr);
            }
            while (!isClose()) {
                for (RedisPool redisPool : badRedisPool) {
                    if (redisPool.isActivity()) {
                        set(redisPool, redisPool.getNumber());
                    }
                }
                try {
                    assert redisPoolTime!=null;
                    Thread.sleep(redisPoolTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 从RedisPool中获取Jedis
     *
     * @return 一个Jedis
     */
    public Jedis getRedis() {
        if (goodsRedisPool.size() == 0) {
            poolToZero();
        }
        Set<Integer> countAll = goodsRedisPool.keySet();

        int allNum = randomRedisPoolNum.nextInt(countAll.size());
        Integer count = countAll.toArray(new Integer[0])[allNum];
        List<RedisPool> redisPools = goodsRedisPool.get(count);
        while (redisPools.size() != 0) {
            int redisNum = randomRedisPoolNum.nextInt(redisPools.size());
            RedisPool redisPool = redisPools.get(redisNum);
            if (redisPool.isClosed()) {
                redisPools.remove(redisPool);
            } else {
                Jedis redis = getRedis(redisPools, redisPool);
                if (redis != null) {
                    return redis;
                }
            }
        }
        goodsRedisPool.remove(count);
        return getRedis();
    }

    /**
     * 如果好的 pool 已经没有了
     * 就会调用此方法
     */
    private void poolToZero() {
        try {
            close();
            logger.error("Redis connector empty，Check that your Redis database is open or that your configuration is correct");
            throw new RedisPoolNullException("Redis connector empty，Check that your Redis database is open or that your configuration is correct");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * 通过地址和端口获取redis
     *
     * @param host 地址
     * @param port 端口
     * @return 一个 jedis 或者 null
     */
    @Override
    public Jedis getRedis(String host, Integer port) {

        for (List<RedisPool> value : goodsRedisPool.values()) {
            for (RedisPool redisPool : value) {
                String host1 = redisPool.getHost();
                Integer port1 = redisPool.getPort();
                if (host.equals(host1) && port.equals(port1)) {
                    return getRedis(value, redisPool);
                }
            }
        }
        return null;
    }

    private Jedis getRedis(List<RedisPool> redisPools, RedisPool redisPool) {
        JedisPool jedisPool = redisPool.getJedisPool();
        try {
            Jedis resource = jedisPool.getResource();
            resource.ping();
            return resource;
        } catch (Exception e) {
            redisPools.remove(redisPool);
            badRedisPool.add(redisPool);
        }
        return null;
    }

    /**
     * 用于关闭RedisPool
     */
    @Override
    public void close() {
        for (List<RedisPool> redisPools : goodsRedisPool.values()) {
            close(redisPools);
        }
        close(badRedisPool);
        setClose(true);
        redisThreadPool.shutdown();
    }

    private void close(Collection<RedisPool> redisPools) {
        for (RedisPool redisPool : redisPools) {
            redisPool.close();
        }
    }



    private void set(RedisPool redisPool, Integer number) {
        List<RedisPool> redisPools = goodsRedisPool.get(number);
        if (redisPools == null) {
            redisPools = new CopyOnWriteArrayList<>();
            redisPools.add(redisPool);
        }
        redisPools.add(redisPool);

        goodsRedisPool.put(number, redisPools);
    }


    /**
     * 判断 redisPool 是否关闭
     *
     * @return false or true
     */
    @Override
    public boolean isClose() {
        return close;
    }

    /**
     * 设置 redisPool的关闭参数
     *
     * @param close true 是关闭 false 是不关闭 默认不关闭
     */
    private void setClose(boolean close) {
        this.close = close;
    }


    static class RedisPool {
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


        boolean isActivity() {
            try {
                Jedis resource = jedisPool.getResource();
                String ping = resource.ping();
                if (ping == null) {
                    return false;
                }
                resource.close();
            } catch (Exception e) {
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

        void close() {
            if (!jedisPool.isClosed()) {
                jedisPool.close();
            }
        }

        boolean isClosed() {
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
