package com.github.ncdhz.redis.net;

import com.github.ncdhz.redis.cache.Redis;
import com.github.ncdhz.redis.cache.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RedisNet默认实现类
 * @author majunlong
 */
public class RedisNetContext implements RedisNet{


    private Logger logger = LoggerFactory.getLogger(RedisNetContext.class);
    
    private static final String URL_SPLIT = ",";

    private static final String ADDRESS_SPLIT = "\\|";

    private static final String HOST_PORT_SPLIT = ":";

    private static final String DEFAULT_REDIS_HOST = "localhost";

    private static final Integer DEFAULT_REDIS_PORT = 6379;



    private Redis redis;


    public static RedisNet getRedisNet(RedisNetConf conf){
        return new RedisNetContext(conf);
    }

    private RedisNetContext(RedisNetConf conf){
        initRedisNet(conf);
        redis = new RedisUtils(conf);
    }



    /**
     * 初始化RedisNet
     * @param conf redis-net 的配置类
     */
    private void initRedisNet(RedisNetConf conf) {
        initNetUrl(conf);
        initDataBase(conf);
        initPassword(conf);
    }

    private void initPassword(RedisNetConf conf) {
        String passwordAll = null;
        try {
            passwordAll = conf.getProperty("redis.password");
            if (passwordAll!=null&&!"".equals(passwordAll)){
                String[]  passwordAddress= passwordAll.split(ADDRESS_SPLIT);
                if (passwordAddress.length!=1){
                    int i = 0;
                    for (List<RedisNetConf.RedisDatabase> redisDatabases : conf) {
                        String passwordAddress1 = passwordAddress[i++];
                        String[] passwordUrl = passwordAddress1.split(URL_SPLIT);
                        if (passwordUrl.length!=1){
                            for (int j = 0; j < redisDatabases.size(); j++) {
                                redisDatabases.get(j).setPassword(passwordUrl[j]);
                            }
                        }else {
                            for (RedisNetConf.RedisDatabase redisDatabase : redisDatabases) {
                                redisDatabase.setPassword(passwordUrl[0]);
                            }
                        }
                    }
                }else {
                    String[] passwordUrl = passwordAll.split(URL_SPLIT);
                    if (passwordUrl.length!=1){
                        for (List<RedisNetConf.RedisDatabase> redisDatabases : conf) {
                            for (int i = 0; i <redisDatabases.size(); i++) {
                                redisDatabases.get(i).setPassword(passwordUrl[i]);
                            }
                        }
                    }else {
                        for (List<RedisNetConf.RedisDatabase> redisDatabases : conf) {
                            for (RedisNetConf.RedisDatabase redisDatabase : redisDatabases) {
                                redisDatabase.setPassword(passwordAll);
                            }
                        }
                    }
                }

            }
        }catch (Exception e){
            initErr("redis.password",passwordAll);
        }
    }

    private void initDataBase(RedisNetConf conf) {
        String databaseAll = null;
        try {
            databaseAll = conf.getProperty("redis.database");
            if (databaseAll!=null&&!"".equals(databaseAll)){
                String[]  databaseAddress= databaseAll.split(ADDRESS_SPLIT);
                if (databaseAddress.length!=1){
                    int i = 0;
                    for (List<RedisNetConf.RedisDatabase> redisDatabases : conf) {
                        String databaseAddress1 = databaseAddress[i++];
                        String[] databaseURL = databaseAddress1.split(URL_SPLIT);
                        if (databaseURL.length!=1){
                            for (int j = 0; j < redisDatabases.size(); j++) {
                                redisDatabases.get(j).setDatabase(Integer.valueOf(databaseURL[j]));
                            }
                        }else {
                            for (RedisNetConf.RedisDatabase redisDatabase : redisDatabases) {
                                redisDatabase.setDatabase(Integer.valueOf(databaseURL[0]));
                            }
                        }
                    }
                }else {
                    String[] databaseUrl = databaseAll.split(URL_SPLIT);
                    if (databaseUrl.length!=1){
                        for (List<RedisNetConf.RedisDatabase> redisDatabases : conf) {
                            for (int i = 0; i <redisDatabases.size(); i++) {
                                redisDatabases.get(i).setDatabase(Integer.valueOf(databaseUrl[i]));
                            }
                        }
                    }else {
                        for (List<RedisNetConf.RedisDatabase> redisDatabases : conf) {
                            for (RedisNetConf.RedisDatabase redisDatabase : redisDatabases) {
                                redisDatabase.setDatabase(Integer.valueOf(databaseAll));
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            initErr("redis.database",databaseAll);
        }
    }

    private void initNetUrl(RedisNetConf conf) {
        String urls = null;
        try{
            urls = conf.getProperty("redis.net.url");
            if (urls==null||"".equals(urls.trim())){
                conf.setRedisDatabase(DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT);
                return;
            }
            String[]  url= urls.trim().split(ADDRESS_SPLIT);
            // 用于把 redis.net.url 的参数存到 conf
            for (String u : url) {
                String[] host = u.trim().split(URL_SPLIT);
                List<RedisNetConf.RedisDatabase> redisDatabases = new CopyOnWriteArrayList<>();
                for (String h : host) {
                    String[] ipAndPort = h.trim().split(HOST_PORT_SPLIT);
                    redisDatabases.add(conf.getRedisDatabase(ipAndPort[0].trim(),Integer.valueOf(ipAndPort[1].trim())));
                }
                conf.setRedisDatabase(redisDatabases);
            }
        }catch (Exception e){
            initErr("redis.net.url",urls);
        }

    }

    private void initErr(String name,String value){
        try {
            logger.error("[{}={}] configure err，URL should include host and port",name,value);
            throw new RedisNetConnectException("["+name+"="+value+"] configure err，URL should include host and port");
        }catch (RedisNetConnectException e){
            e.printStackTrace();
            System.exit(0);
        }
    }


    @Override
    public String set(String key, String value) {
        return (String) redis.set(key,value);
    }

    @Override
    public String get(String key) {
        return (String) redis.get(key);
    }

    @Override
    public void close() {
        redis.close();
    }
}
