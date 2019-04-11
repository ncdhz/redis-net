package com.github.ncdhz.redis.util;

import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

public class RedisPoolUtils {
    /**
     * 存放未失效的JedisPool
     */
    private static List<List<RedisPool>> goodsRedisPool = new ArrayList<>();
    /**
     * 存放失效的JedisPool
     */
    private static List<List<RedisPool>> badRedisPool = new ArrayList<>();

    /**
     * 用于计数JedisPool 的编号
     */
    private static int count = 1;

    public static void set(List<JedisPool> jedisPools){
        List<RedisPool> redisPools = new ArrayList<>();
        for (JedisPool jedisPool : jedisPools) {
            redisPools.add(new RedisPool(count,jedisPool));
        }
        goodsRedisPool.add(redisPools);
        count++;
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

        public RedisPool(int number, JedisPool jedisPool) {
            this.number = number;
            this.jedisPool = jedisPool;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public JedisPool getJedisPool() {
            return jedisPool;
        }

        public void setJedisPool(JedisPool jedisPool) {
            this.jedisPool = jedisPool;
        }
    }
}
