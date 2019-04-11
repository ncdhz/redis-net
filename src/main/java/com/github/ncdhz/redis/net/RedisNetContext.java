package com.github.ncdhz.redis.net;

import com.github.ncdhz.redis.util.RedisPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    
    public RedisNetContext(RedisNetConf conf){
        initRedisNet(conf);
    }

    /**
     * 初始化RedisNet
     */
    private void initRedisNet(RedisNetConf redisNetConf) {
        String urls = (String) conf.get("redis.net.url");
        if (urls==null||"".equals(urls.trim())){
            logger.error("[redis.net.url] is null or '',Please configure reasonably [redis.net.url]");
            throw new RedisNetConnectException("[redis.net.url] is null or '',Please configure reasonably [redis.net.url]");
        }
        String[]  url= urls.trim().split(HOST_SPLIT);
        for (String u : url) {
            String[] host = u.trim().split(URL_SPLIT);
            List<JedisPool> jedisPools = new ArrayList<>();
            for (String h : host) {
                String[] ipAndPort = h.trim().split(HOST_PORT_SPLIT);
                if (ipAndPort.length!=2){
                    logger.error("[redis.net.url] configure err，URL should include host and port");
                    throw new RedisNetConnectException("[redis.net.url] configure err，URL should include host and port");
                }
                JedisPool jedisPool = new JedisPool(redisNetConf, ipAndPort[0].trim(),Integer.valueOf(ipAndPort[1].trim()));
                jedisPools.add(jedisPool);
            }
            RedisPoolUtils.set(jedisPools);
        }
    }

    @Override
    public String set(String key, String value) {
        return null;
    }
}
