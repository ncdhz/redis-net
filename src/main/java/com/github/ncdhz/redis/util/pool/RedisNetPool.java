package com.github.ncdhz.redis.util.pool;

import com.github.ncdhz.redis.net.RedisThreadPool;
import com.github.ncdhz.redis.net.RedisConf;
import com.github.ncdhz.redis.net.RedisDatabase;
import com.github.ncdhz.redis.net.RedisDatabaseConf;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author majunlong
 */
public class RedisNetPool implements RedisPool {

    private RedisConf conf;

    private ThreadPoolExecutor poolExecutor;
    /**
     * 存放未失效的JedisPool
     */
    private Map<Integer, List<RedisDataPool>> goodsRedisPool = new ConcurrentHashMap<>();
    /**
     * 检测密码是否为空
     */
    private static final String PASSWORD_NULL = "null";
    /**
     * 存放失效的JedisPool
     */
    private List<RedisDataPool> badRedisPool = new CopyOnWriteArrayList<>();
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

    private Integer databaseTimeOut;

    public RedisNetPool(RedisConf conf, ThreadPoolExecutor poolExecutor) {
        this.conf = conf;

        this.poolExecutor = poolExecutor;

        databaseTimeOut = initDatabaseTimeOut(conf);

        RedisDatabaseConf databaseConf = (RedisDatabaseConf) conf;

        initRedisPool(databaseConf.getAllRedisDatabase());

        checkBadPool();
        checkGoodPool();
    }

    private Integer initDatabaseTimeOut(RedisConf conf){
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
     * @param redisDatabases redis Database 一些基本配置
     */
    public void initRedisPool(List<List<RedisDatabase>> redisDatabases) {
        for (List<RedisDatabase> redisDatabase : redisDatabases) {
            initRedisNetPool(redisDatabase);
        }
    }
    private void initRedisNetPool(List<RedisDatabase> redisDatabases){
        List<RedisDataPool> redisPools = new CopyOnWriteArrayList<>();
        for (RedisDatabase redisDatabase : redisDatabases) {
            Integer port = redisDatabase.getPort();
            String host = redisDatabase.getHost();
            Integer database = redisDatabase.getDatabase();
            String password = redisDatabase.getPassword();
            password = passwordIsNull(password)?null:password;
            database = database==null?0:database;

            JedisPool jedisPool =new JedisPool((GenericObjectPoolConfig) conf,host,port,databaseTimeOut,password,database);
            RedisDataPool redisDataPool = new RedisDataPool(count, jedisPool, host, port);
            redisPools.add(redisDataPool);

        }
        goodsRedisPool.put(count++, redisPools);
    }

    private boolean passwordIsNull(String password){
        return password==null||"".equals(password)||password.toLowerCase().equals(PASSWORD_NULL);
    }

    /**
     * 用于检查好的连接池里面的数据是否已经挂掉
     */
    private void checkGoodPool() {
        poolExecutor.execute(() -> {
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

            while (!close) {
                for (List<RedisDataPool> value : goodsRedisPool.values()) {
                    for (RedisDataPool redisDataPool : value) {
                        getRedis(value, redisDataPool);
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
        poolExecutor.execute(() -> {
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
            while (!close) {
                for (RedisDataPool redisDataPool : badRedisPool) {
                    if (redisDataPool.isActivity()) {
                        set(redisDataPool, redisDataPool.getNumber());
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
    @Override
    public Jedis getRedis() {
        if (goodsRedisPool.size() == 0) {
            poolToZero();
        }
        Set<Integer> countAll = goodsRedisPool.keySet();

        int allNum = randomRedisPoolNum.nextInt(countAll.size());
        Integer count = countAll.toArray(new Integer[0])[allNum];
        List<RedisDataPool> redisPools = goodsRedisPool.get(count);
        while (redisPools.size() != 0) {
            int redisNum = randomRedisPoolNum.nextInt(redisPools.size());
            RedisDataPool redisDataPool = redisPools.get(redisNum);
            if (redisDataPool.isClosed()) {
                redisPools.remove(redisDataPool);
            } else {
                Jedis redis = getRedis(redisPools, redisDataPool);
                if (redis != null) {
                    return redis;
                }
            }
        }
        goodsRedisPool.remove(count);
        return getRedis();
    }

    /**
     * 如果好的 RedisDataPool 已经没有了
     * 就会调用此方法
     */
    private void poolToZero() {
        try {
            throw new RedisPoolNullException("Redis connector empty，Check that your Redis DatabaseConf is open or that your configuration is correct");
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

        for (List<RedisDataPool> value : goodsRedisPool.values()) {
            for (RedisDataPool redisDataPool : value) {
                String host1 = redisDataPool.getHost();
                Integer port1 = redisDataPool.getPort();
                if (host.equals(host1) && port.equals(port1)) {
                    return getRedis(value, redisDataPool);
                }
            }
        }
        return null;
    }

    private Jedis getRedis(List<RedisDataPool> redisPools, RedisDataPool redisDataPool) {
        JedisPool jedisPool = redisDataPool.getJedisPool();
        try {
            Jedis resource = jedisPool.getResource();
            resource.ping();
            return resource;
        } catch (Exception e) {
            redisPools.remove(redisDataPool);
            badRedisPool.add(redisDataPool);
        }
        return null;
    }

    /**
     * 用于关闭RedisPool
     */
    @Override
    public void close() {
        close = true;
        for (List<RedisDataPool> redisPools : goodsRedisPool.values()) {
            close(redisPools);
        }
        close(badRedisPool);
    }

    private void close(Collection<RedisDataPool> redisPools) {
        for (RedisDataPool redisDataPool : redisPools) {
            redisDataPool.close();
        }
    }



    private void set(RedisDataPool redisDataPool, Integer number) {
        List<RedisDataPool> redisPools = goodsRedisPool.get(number);
        if (redisPools == null) {
            redisPools = new CopyOnWriteArrayList<>();
            redisPools.add(redisDataPool);
        }
        redisPools.add(redisDataPool);

        goodsRedisPool.put(number, redisPools);
    }


    class RedisDataPool {
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


        RedisDataPool(int number, JedisPool jedisPool, String host, Integer port) {
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
