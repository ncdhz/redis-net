package com.github.ncdhz.redis.net;


import com.github.ncdhz.redis.handler.RedisNetConnectException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author majunlong
 */
public class RedisNetInit implements RedisInit{

    private RedisDatabaseConf databaseConf;

    private RedisConf conf;

    private static final String URL_SPLIT = ",";

    private static final String ADDRESS_SPLIT = "\\|";

    private static final String HOST_PORT_SPLIT = ":";

    private static final String DEFAULT_REDIS_HOST = "localhost";

    private static final Integer DEFAULT_REDIS_PORT = 6379;

    public RedisNetInit(RedisConf conf) {
        this.databaseConf = (RedisDatabaseConf) conf;
        this.conf = conf;
    }

    @Override
    public RedisInit initPassword() {
        if (!databaseConf.isInit()){
            String passwordAll = null;
            try {
                passwordAll = conf.getProperty("redis.password");
                if (passwordAll!=null&&!"".equals(passwordAll)){
                    String[]  passwordAddress= passwordAll.split(ADDRESS_SPLIT);
                    if (passwordAddress.length!=1){
                        int i = 0;
                        for (List<RedisDatabase> redisDatabases : databaseConf) {
                            String passwordAddress1 = passwordAddress[i++];
                            String[] passwordUrl = passwordAddress1.split(URL_SPLIT);
                            if (passwordUrl.length!=1){
                                for (int j = 0; j < redisDatabases.size(); j++) {
                                    redisDatabases.get(j).setPassword(passwordUrl[j]);
                                }
                            }else {
                                for (RedisDatabase redisDatabase : redisDatabases) {
                                    redisDatabase.setPassword(passwordUrl[0]);
                                }
                            }
                        }
                    }else {
                        String[] passwordUrl = passwordAll.split(URL_SPLIT);
                        if (passwordUrl.length!=1){

                            for (List<RedisDatabase> redisDatabases : databaseConf) {
                                for (int i = 0; i < redisDatabases.size(); i++) {
                                    redisDatabases.get(i).setPassword(passwordUrl[i]);
                                }
                            }
                        }else {

                            for (List<RedisDatabase> redisDatabases : databaseConf) {
                                for (RedisDatabase redisDatabase : redisDatabases) {
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
        return this;
    }

    @Override
    public RedisInit initDatabase() {
        if (!databaseConf.isInit()){
            String databaseAll = null;
            try {
                databaseAll = conf.getProperty("redis.database");
                if (databaseAll!=null&&!"".equals(databaseAll)){
                    String[]  databaseAddress= databaseAll.split(ADDRESS_SPLIT);
                    if (databaseAddress.length!=1){
                        int i = 0;
                        for (List<RedisDatabase> redisDatabases : databaseConf) {
                            String databaseAddress1 = databaseAddress[i++];
                            String[] databaseUrl = databaseAddress1.split(URL_SPLIT);
                            if (databaseUrl.length!=1){
                                for (int j = 0; j < redisDatabases.size(); j++) {
                                    redisDatabases.get(j).setDatabase(Integer.valueOf(databaseUrl[j]));
                                }
                            }else {
                                for (RedisDatabase redisDatabase : redisDatabases) {
                                    redisDatabase.setDatabase(Integer.valueOf(databaseUrl[0]));
                                }
                            }
                        }
                    }else {
                        String[] passwordUrl = databaseAll.split(URL_SPLIT);
                        if (passwordUrl.length!=1){

                            for (List<RedisDatabase> redisDatabases : databaseConf) {
                                for (int i = 0; i < redisDatabases.size(); i++) {
                                    redisDatabases.get(i).setDatabase(Integer.valueOf(passwordUrl[i]));
                                }
                            }
                        }else {

                            for (List<RedisDatabase> redisDatabases : databaseConf) {
                                for (RedisDatabase redisDatabase : redisDatabases) {
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
        return this;
    }

    @Override
    public RedisInit initNetUrl() {
        if (!databaseConf.isInit()){
            String urls = null;
            try{
                urls = conf.getProperty("redis.net.url");
                if (urls==null||"".equals(urls.trim())){
                    databaseConf.setRedisDatabase(DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT);
                    return this;
                }
                String[]  url= urls.trim().split(ADDRESS_SPLIT);
                // 用于把 redis.net.url 的参数存到 conf
                for (String u : url) {
                    String[] host = u.trim().split(URL_SPLIT);
                    List<RedisDatabase> redisDatabases = new CopyOnWriteArrayList<>();
                    for (String h : host) {
                        String[] ipAndPort = h.trim().split(HOST_PORT_SPLIT);
                        redisDatabases.add(databaseConf.getRedisDatabase(ipAndPort[0].trim(),Integer.valueOf(ipAndPort[1].trim())));
                    }
                    databaseConf.setRedisDatabase(redisDatabases);
                }
            }catch (Exception e){
                initErr("redis.net.url",urls);
            }
        }
        return this;
    }

    private void initErr(String name,String value){
        try {
            throw new RedisNetConnectException("["+name+"="+value+"] configure err，URL should include host and port");
        }catch (RedisNetConnectException e){
            e.printStackTrace();
            System.exit(0);
        }
    }
}
