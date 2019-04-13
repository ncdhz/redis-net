package com.github.ncdhz.redis.util;

import java.util.concurrent.*;

/**
 * RedisThreadPool
 * 所有redis需要的线程都从里面获取
 */
public class RedisThreadPool extends ThreadPoolExecutor{

    public RedisThreadPool(int num) {
        super(num,num,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
    }
}
