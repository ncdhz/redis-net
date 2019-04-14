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
public class RedisNetConf extends JedisPoolConfig implements Iterable<List<RedisNetConf.RedisDatabase>>{

    private Properties conf= new Properties();

    private List<List<RedisDatabase>> redisDatabase = new CopyOnWriteArrayList<>();
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
    public RedisNetConf set(String key,String value){
        conf.put(key,value);
        return this;
    }

    public Object get(String key) {
        return conf.get(key);
    }

    public void put(String key, Object value) {
        conf.put(key,value);
    }

    public List<List<RedisDatabase>> getAllRedisDatabase(){
        return redisDatabase;
    }

    public String getProperty(String key) {
        return conf.getProperty(key);
    }

    public void setProperty(String key, String value) {
        conf.setProperty(key,value);
    }

    public void setRedisDatabase(RedisDatabase redisDatabase){
        CopyOnWriteArrayList<RedisDatabase> database = new CopyOnWriteArrayList<>();
        database.add(redisDatabase);
        this.redisDatabase.add(database);
    }

    public void setRedisDatabase(List<RedisDatabase> redisDatabases){
        redisDatabase.add(redisDatabases);
    }

    public RedisDatabase getRedisDatabase(String host,Integer port){
        return new RedisDatabase(host,port);
    }

    public List<List<String[]>> getAllHostAndPort() {
        List<List<String[]>> allRedisDatabase = new CopyOnWriteArrayList<>();
        for (List<RedisDatabase> redisDatabases : redisDatabase) {
            List<String[]> allHostAllPortToOne = new CopyOnWriteArrayList<>();
            for (RedisDatabase database : redisDatabases) {
                String[] hostAndPort = new String[]{database.getHost(),database.getPort().toString()};
                allHostAllPortToOne.add(hostAndPort);
            }
            allRedisDatabase.add(allHostAllPortToOne);
        }
        return allRedisDatabase;
    }

    public RedisDatabase getRedisDatabase(){
        return new RedisDatabase();
    }

    public void setRedisDatabase(String host, Integer port) {
        setRedisDatabase(new RedisDatabase(host,port));
    }



    @Override
    public Iterator<List<RedisDatabase>> iterator() {
        return redisDatabase.iterator();
    }

    public class RedisDatabase{

        private String host;

        private Integer port;

        private Integer database;

        private String password;

        public RedisDatabase(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        public RedisDatabase() {}

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public void setDatabase(Integer database) {
            this.database = database;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public Integer getDatabase() {
            return database;
        }

        public String getPassword() {
            return password;
        }
    }
}
