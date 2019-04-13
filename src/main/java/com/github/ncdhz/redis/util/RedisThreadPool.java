package com.github.ncdhz.redis.util;

import java.util.concurrent.*;

/**
 * RedisThreadPool
 * 所有redis需要的线程都从里面获取
 */
public class RedisThreadPool extends ThreadPoolExecutor{

    private static RedisThreadPool redisThreadPool= new RedisThreadPool(10,10,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());

    private RedisThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public static RedisThreadPool getRedisThreadPool(){
        return redisThreadPool;
    }
}
