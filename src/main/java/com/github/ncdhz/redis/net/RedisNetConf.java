package com.github.ncdhz.redis.net;

import redis.clients.jedis.JedisPoolConfig;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * redis-net 的配置类
 * @author majunlong
 */
public class RedisNetConf extends JedisPoolConfig implements RedisConf,RedisDatabaseConf{

    private Properties conf= new Properties();

    private List<List<RedisDatabase>> redisDatabase = new CopyOnWriteArrayList<>();

    private boolean init = false;
    /**
     * 初始化配置
     */
    public RedisNetConf(){
        super();
    }

    public RedisNetConf(String key,String value){
        super();
        conf.put(key,value);
    }

    public RedisNetConf(Properties conf){
        super();
        this.conf = conf;
    }

    /**
     * 设置参数
     * @param key 参数名
     * @param value 参数值
     * @return 返回配置对象
     */
    @Override
    public RedisConf set(String key, String value){
        conf.put(key,value);
        return this;
    }

    @Override
    public Object get(String key) {
        return conf.get(key);
    }

    @Override
    public String getProperty(String key) {
        return conf.getProperty(key);
    }

    @Override
    public List<List<RedisDatabase>> getAllRedisDatabase(){
        return redisDatabase;
    }



    @Override
    public void setRedisDatabase(RedisDatabase redisDatabase){
        List<RedisDatabase> redisDatabases = new CopyOnWriteArrayList<>();
        redisDatabases.add(redisDatabase);
        setRedisDatabase(redisDatabases);
    }

    @Override
    public void setRedisDatabase(List<RedisDatabase> redisDatabases){
        this.redisDatabase.add(redisDatabases);
    }

    @Override
    public RedisDatabase getRedisDatabase(String host, Integer port){
        return new RedisNetRedisDatabase(host,port);
    }

    @Override
    public List<List<String[]>> getAllHostAndPort() {
        List<List<String[]>> allDatabaseConf = new CopyOnWriteArrayList<>();

        for (List<RedisDatabase> redisDatabases : redisDatabase) {
            List<String[]> allHostAllPortToOne = new CopyOnWriteArrayList<>();
            for (RedisDatabase redisDatabase : redisDatabases) {
                allHostAllPortToOne.add(redisDatabase.getHostAndPort());
            }
            allDatabaseConf.add(allHostAllPortToOne);
        }
        return allDatabaseConf;
    }

    @Override
    public RedisDatabase getRedisDatabase(){
        return new RedisNetRedisDatabase();
    }

    @Override
    public void setRedisDatabase(String host, Integer port) {
        setRedisDatabase(new RedisNetRedisDatabase(host,port));
    }

    @Override
    public Iterator<List<RedisDatabase>> iterator() {
        return redisDatabase.iterator();
    }

    @Override
    public Boolean isInit(){
        return init;
    }

    @Override
    public void setInit(boolean init) {
        this.init = init;
    }
}
