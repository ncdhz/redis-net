package com.github.ncdhz.redis.net;

import com.github.ncdhz.redis.util.RedisPoolUtils;
import com.github.ncdhz.redis.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RedisNet默认实现类
 * @author majunlong
 */
public class RedisNetContext implements RedisNet {

    private Properties conf= System.getProperties();

    private Logger logger = LoggerFactory.getLogger(RedisNetContext.class);
    
    private static final String URL_SPLIT = ",";

    private static final String HOST_SPLIT = "\\|";

    private static final String HOST_PORT_SPLIT = ":";
    
    RedisNetContext(RedisNetConf conf){
        initRedisNet(conf);
    }

    /**
     * 初始化RedisNet
     */
    private void initRedisNet(JedisPoolConfig redisNetConf) {
        String urls = (String) conf.get("redis.net.url");
        if (urls==null||"".equals(urls.trim())){
            logger.error("[redis.net.url] is null or '',Please configure reasonably [redis.net.url]");
            throw new RedisNetConnectException("[redis.net.url] is null or '',Please configure reasonably [redis.net.url]");
        }
        String[]  url= urls.trim().split(HOST_SPLIT);
        // 用于把 redis.net.url 的参数存到 System
        List<List<String[]>> redisNetUrl = new ArrayList<>();
        for (String u : url) {
            String[] host = u.trim().split(URL_SPLIT);

            List<String[]> hostAndPort = new CopyOnWriteArrayList<>();
            for (String h : host) {
                String[] ipAndPort = h.trim().split(HOST_PORT_SPLIT);
                if (ipAndPort.length!=2){
                    logger.error("[redis.net.url] configure err，URL should include host and port");
                    throw new RedisNetConnectException("[redis.net.url] configure err，URL should include host and port");
                }
                hostAndPort.add(ipAndPort);

            }

            redisNetUrl.add(hostAndPort);
            // 在redisPool 里面添加一套 （可以是去中心分布式的） redis 数据库
            RedisPoolUtils.set(hostAndPort,redisNetConf);
        }
        conf.put("redis.net.url",redisNetUrl);
    }

    @Override
    public String set(String key, String value) {
        return (String) RedisUtils.set(key,value);
    }

    @Override
    public String get(String key) {
        return RedisUtils.get(key);
    }

    @Override
    public void close() {
        RedisPoolUtils.close();
    }
}
